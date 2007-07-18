/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2007 The International Cooperation for the Integration of 
 * Processes in  Prepress, Press and Postpress (CIP4).  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:  
 *       "This product includes software developed by the
 *        The International Cooperation for the Integration of 
 *        Processes in  Prepress, Press and Postpress (www.cip4.org)"
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "CIP4" and "The International Cooperation for the Integration of 
 *    Processes in  Prepress, Press and Postpress" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written 
 *    permission, please contact info@cip4.org.
 *
 * 5. Products derived from this software may not be called "CIP4",
 *    nor may "CIP4" appear in their name, without prior written
 *    permission of the CIP4 organization
 *
 * Usage of this software in commercial products is subject to restrictions. For
 * details please consult info@cip4.org.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE INTERNATIONAL COOPERATION FOR
 * THE INTEGRATION OF PROCESSES IN PREPRESS, PRESS AND POSTPRESS OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the The International Cooperation for the Integration 
 * of Processes in Prepress, Press and Postpress and was
 * originally based on software 
 * copyright (c) 1999-2001, Heidelberger Druckmaschinen AG 
 * copyright (c) 1999-2001, Agfa-Gevaert N.V. 
 *  
 * For more information on The International Cooperation for the 
 * Integration of Processes in  Prepress, Press and Postpress , please see
 * <http://www.cip4.org/>.
 *  
 * 
 */
package org.cip4.bambi;

import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFMessageService;
import org.cip4.jdflib.jmf.JDFQuery;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFSubscription;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.util.ContainerUtil;


/**
 *
 * @author  rainer
 *
 *
 * @web:servlet-init-param	name="" 
 *									value=""
 *									description=""
 *
 * @web:servlet-mapping url-pattern="/FixJDFServlet"
 */
public class JMFHandler implements IMessageHandler
{
    
    private static Log log = LogFactory.getLog(JMFHandler.class.getName());

    /**
     * 
     */
    private static final long serialVersionUID = -8902151736245089033L;
    private HashMap messageMap; // key = type , value = IMessageHandler
    private HashMap familyMap; // key = type , value = families handled
    private HashMap subscriptionMap; // key = type , value = subscriptions handled
       
    /**
     * 
     *
     */
    public JMFHandler()
    {
        super();
        messageMap=new HashMap();
        familyMap=new HashMap();
        subscriptionMap=new HashMap();
        addHandler(EnumType.KnownMessages, new EnumFamily[]{EnumFamily.Query},this);
    }
    /**
     * add a message handler
     * @param typ - the message Type
     * @param families an array of families that the handler handles
     * @param handler the handler associated with the event
     */
    public void addHandler(EnumType typ, EnumFamily[] families, IMessageHandler handler)
    {
        messageMap.put(typ, handler);
        familyMap.put(typ, families);
    }
    /**
     * @param typ the message type
     * @param family the family
     * @return the handler, null if none exists
     */
    private IMessageHandler getHandler(EnumType typ, EnumFamily family)
    {
        IMessageHandler messageHandler=(IMessageHandler) messageMap.get(typ);
        if(messageHandler!=null)
        {
            EnumFamily[]fams=(EnumFamily[]) familyMap.get(typ);
            if(fams==null || !ArrayUtils.contains(fams, family))
                messageHandler=null;
        }
        return messageHandler;
    }
    /**
     * @param knownMessages
     * @param families
     * @param handler
     */
    public void addSubscriptionHandler(EnumType typ, IMessageHandler handler)
    {
        subscriptionMap.put(typ, handler);
    }
    /**
     * the big processing dipatcher
     * 
     * @param doc
     * @return
     */
    public JDFDoc processJMF(JDFDoc doc)
    {
       JDFJMF jmf=doc.getJMFRoot();
       JDFJMF jmfResp=jmf.createResponse();
       VElement vMess=jmf.getMessageVector(null,null);
       for(int i=0;i<vMess.size();i++)
       {
           JDFMessage m=(JDFMessage) vMess.elementAt(i);
           String id=m.getID();
           
           JDFResponse mResp=(JDFResponse) (id==null ? null : jmfResp.getChildWithAttribute(ElementName.RESPONSE,AttributeName.REFID, null, id, 0, true));
           if(mResp==null)
               log.error("??? "+id+" "+jmfResp);
           processMessage(m,mResp);
       }   
       return new JDFDoc(jmfResp.getOwnerDocument());
    }

    /**
     * fill the response with values derived from the message
     * @param m
     * @param resp
     */
    private void processMessage(JDFMessage m, JDFResponse resp)
    {
        if(m==null)
            return;
        EnumFamily fam=m.getFamily();
        if(EnumFamily.Query.equals(fam))
        {
            JDFQuery q=(JDFQuery)m;
            JDFSubscription subscript=q.getSubscription();
            if(subscript!=null)
            {
                processSubscription(q,resp,subscript);
                return;
            }
        }
        IMessageHandler handler=getHandler(m.getEnumType(), fam);
        boolean handled=handler!=null;
        if(handler!=null)
            handled=handler.handleMessage(m, resp);
        if(!handled)
            unhandledMessage(m,resp);
    }

      /**
       * standard handler for unimplemented messages
     * @param m
     * @param resp
     */
    private void unhandledMessage(JDFMessage m, JDFResponse resp)
    {
        log.info("unhandled Message: "+m.getType());
        if(resp==null)
            return;
       resp.setReturnCode(5);
       resp.setErrorText("Message not implemented: "+m.getType()+"; Family: "+m.getFamily().getName());        
    }
    /**
     * @param q
     * @param resp
     * @param subscript
     */
    private void processSubscription(JDFQuery q, JDFMessage resp, JDFSubscription subscript)
    {
        // TODO Auto-generated method stub
        
    }
    /* (non-Javadoc)
     * @see org.cip4.bambi.IMessageHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFMessage)
     */
    public boolean handleMessage(JDFMessage m, JDFMessage resp)
    {
        if(m==null || resp==null)
            return false;
        log.debug("Handling"+m.getType());
        EnumType typ=m.getEnumType();
        if(EnumType.KnownMessages.equals(typ))
            return handleKnownMessages(m, resp);
        
        return false;
    }
    /**
     * @return
     */
    private boolean handleKnownMessages(JDFMessage m, JDFMessage resp)
    {
        if(m==null)
            return false;
        if(!EnumFamily.Query.equals(m.getFamily()))
            return false;
        
       Iterator it=messageMap.keySet().iterator();
       while(it.hasNext())
       {
           EnumType typ=(EnumType)it.next();
           log.debug("Known Message: "+typ.getName());
           JDFMessageService ms=resp.appendMessageService();
           ms.setType(typ);
           ms.setFamilies(ContainerUtil.toVector((EnumFamily[])familyMap.get(typ)));     
           if(subscriptionMap.get(typ)!=null)
               ms.setPersistent(true);
       }
       return true;
    }
    
}
