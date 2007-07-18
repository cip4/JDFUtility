/**
 * 
 */
package org.cip4.bambi;

import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.jmf.JDFCommand;
import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.jmf.JDFResponse;

/**
 * @author prosirai
 *
 */
public interface IQueueProcessor
{
    /**
     * get the next waiting entry
     * @return
     */
    public IQueueEntry getNextEntry();
    
    /**
     * get the jdf representation of this queue
     * @return JDFQueue the jdf representation of this queue
     */
    public JDFQueue getQueue();
    
    /**
     * add a new entry to the queue
     * 
     * @param sumitQueueEntry queuesubmission command
     * @param theJDF the referenced jdf doc
     * @return 
     */
    public JDFResponse addEntry(JDFCommand sumitQueueEntry, JDFDoc theJDF);
    
    /**
     * updated an entry in the queue 
     * @param queueEntryID the queuentryid to update
     * @param status the queuentry status
     */
    public void updateEntry(String queueEntryID, EnumQueueEntryStatus status);
    
    public void addListener(Object o);

}
