package org.cip4.jdfutility;

import java.io.BufferedOutputStream;
import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.XMLDoc;
import org.cip4.jdflib.util.CPUTimer;
import org.cip4.jdflib.util.JDFDate;
import org.cip4.jdflib.util.MemorySpy;
import org.cip4.jdflib.util.MemorySpy.MemScope;
import org.cip4.jdflib.util.UrlUtil;
import org.cip4.jdfutility.html.HTMLUtil;

/**
 * bookkeeping 
  * @author Rainer Prosi, Heidelberger Druckmaschinen *
 */
public abstract class ServletCall
{
	/**
	 * 
	 */
	protected final HttpServletRequest request;
	protected final HttpServletResponse response;
	protected UtilityServlet parentServlet;
	protected Log log;

	/**
	 * 
	 * @param utilityServlet
	 * @param request
	 * @param response
	 */
	public ServletCall(UtilityServlet utilityServlet, final HttpServletRequest request, HttpServletResponse response)
	{
		super();
		timer = new CPUTimer(true);
		id = KElement.uniqueID(0);
		this.request = request;
		this.response = response;
		parentServlet = utilityServlet;
		log = LogFactory.getLog(getClass());
		doc = null;

	}

	/**
	 * 
	 * @return
	 */
	public long getTimeProcessed()
	{
		long deltaT = System.currentTimeMillis() - timer.getCreationTime();
		return deltaT;
	}

	/**
	 * 
	 */
	public void complete()
	{
		long timeProcessed = getTimeProcessed();
		parentServlet.tTotal += timeProcessed;
		parentServlet.tMax = Math.max(parentServlet.tMax, timeProcessed);
		long t = timer.getTotalCPUTime() / 1000;// micros is ok
		parentServlet.tCPUMax = Math.max(parentServlet.tCPUMax, t);
		if (t > 0)
			parentServlet.tCPUTotal += t;
	}

	/**
	 * 
	 */
	protected CPUTimer timer;
	String id;
	int contentLength;
	protected XMLDoc doc;
	protected long requestLen;

	/**
	 * @return
	 */
	public KElement getHTMLRoot()
	{
		return doc == null ? null : doc.getRoot();
	}

	/**
	 */
	protected void finalizeGet()
	{
		long deltaT = getTimeProcessed();
		complete();
		if (doc != null)
		{
			KElement body = getHTMLRoot().getCreateElement("body");
			body.appendElement("h2").setText("Summary of All requests");
			HTMLUtil.appendLine(body, "# Get requests: " + parentServlet.numGet);
			HTMLUtil.appendLine(body, "# Post requests: " + parentServlet.numPost);
			HTMLUtil.appendLine(body, "# MB Processed: " + (parentServlet.requestLen / 10000 / 100.));
			HTMLUtil.appendLine(body, "Time Spent (milliSeconds): " + deltaT + " Total time(milliseconds): " + parentServlet.tTotal / 1. + " Max time(milliseconds): "
					+ parentServlet.tMax + " Average: " + (parentServlet.tTotal / (parentServlet.numGet + parentServlet.numPost)));
			HTMLUtil.appendLine(body, "CPU Time Spent (milliSeconds): " + timer.getTotalCPUTime() / 10000 / 100. + " Total CPU time (milliseconds): " + parentServlet.tCPUTotal
					/ 1000. + " Max CPU time(milliseconds): " + parentServlet.tCPUMax / 10 / 100. + " Average(milliSeconds): "
					+ (parentServlet.tCPUTotal / (parentServlet.numGet + parentServlet.numPost) / 1000.0));
			MemorySpy spy = new MemorySpy();
			HTMLUtil.appendLine(body, null);
			HTMLUtil.appendLine(body, "Memory used (MB): " + (spy.getHeapUsed(MemScope.current) / 10000 / 100.0));
			HTMLUtil.appendLine(body, "Memory comitted (MB): " + (spy.getHeapUsed(MemScope.commit) / 10000 / 100.0));
			HTMLUtil.appendLine(body, "Memory free (MB): " + ((spy.getHeapUsed(MemScope.commit) - spy.getHeapUsed(MemScope.current)) / 10000 / 100.0));
			HTMLUtil.appendLine(body, "Permanent Memory used (MB): " + (spy.getPermGen(MemScope.current) / 10000 / 100.0));
			body.appendElement("hr");
			HTMLUtil.appendLine(body, new JDFDate().getFormattedDateTime("MMM' 'dd' 'yyyy' - 'HH:mm:ss"));
			body.setXPathAttribute("font/@size", "-1");
			body.setXPathAttribute("font/@color", "gray");
			body.getElement("font").setText("- active since: " + parentServlet.startDate.getFormattedDateTime("MMM' 'dd' 'yyyy' - 'HH:mm:ss"));
			try
			{
				ServletOutputStream outputStream = response.getOutputStream();
				response.setContentType(UrlUtil.TEXT_HTML);
				BufferedOutputStream bos = new BufferedOutputStream(outputStream);
				doc.write2Stream(bos, 2, false);
				bos.flush();
				bos.close();
				bos = null;
				outputStream = null;
			}
			catch (IOException x)
			{
				// nop
			}
		}
		else
		{
			parentServlet.log.error("no output print writer, bailing out");
		}
		parentServlet.log.info("Processed Get " + parentServlet.getServletInfo() + " request. ID=" + id + " Time required:" + deltaT + " Total time:" + parentServlet.tTotal);
	}

	/**
	 */
	protected void finalizePost()
	{
		long deltaT = getTimeProcessed();
		complete();
		log.info("Processed Post " + parentServlet.getServletInfo() + " request. ID=" + id + " Time required:" + deltaT + " Total time:" + parentServlet.tTotal);
	}

	/**
	 * @param message 
	 *  
	 */
	protected void errorResponse(String message)
	{
		setupGet(request, response);
		response.setContentType(UrlUtil.TEXT_HTML);
		KElement root = getHTMLRoot();
		KElement body = root.getCreateElement("body");
		body.appendElement("hr");
		body.appendElement("h1").setText("Error Message");
		body.appendText(message);
		body.appendElement("hr");
	}

	/**
	 * @throws IOException 
	 * @throws ServletException 
	 *  
	 */
	protected void processGet() throws IOException, ServletException
	{
		final String contentType = request.getContentType();
		String rHost = request.getRemoteHost();
		int rPort = request.getRemotePort();
		contentLength = request.getContentLength();
		KElement root = getHTMLRoot();
		KElement body = root.getCreateElement("body");
		body.appendElement("h1").setText("Request Details");
		body.appendElement("h2").setText("Details for this request");
		HTMLUtil.appendLine(body, "Content Type: " + contentType);
		HTMLUtil.appendLine(body, "Content Length: " + contentLength);
		HTMLUtil.appendLine(body, "Request URI: " + request.getRequestURL().toString());
		HTMLUtil.appendLine(body, "Remote host: " + rHost + ":" + rPort);
	}

	/**
	 * @throws IOException 
	 * @throws ServletException 
	 * 
	 */
	protected abstract void processPost() throws ServletException, IOException;

	/**
	 * @param request
	 * @param response
	 */
	protected void setupGet(final HttpServletRequest request, final HttpServletResponse response)
	{
		if (doc != null)
			return;
		doc = HTMLUtil.createHTMLRoot().getOwnerDocument_KElement();
		KElement root = doc.getRoot();
		String css = parentServlet.getCssURL(request);
		HTMLUtil.setCSS(root, css);
		HTMLUtil.setTitle(root, parentServlet.getServletInfo());
	}

}