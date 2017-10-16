'''
Created on Oct 15, 2017

@author: dpark
'''
from datetime import datetime
import json

from com.netcrest.pado.rpc.client.biz.temporal_biz import TemporalBiz
from com.netcrest.pado.rpc.rpc_shared import RpcShared
from com.netcrest.pado.rpc.util.class_util import get_class_name


class Dna(RpcShared):
    '''
    A parent class of all DNA classes.
    
    All DNA classes must inherit this class and provide the following attributes:
        pado: Pado object. This instance is for the local application use only. It
            is typically set by DNAs that are executed locally. It has no use in
            the data node.
    '''
    
    pado = None
    
    def __init__(self, pado=None):
        self.pado = pado
    
    def getCurrentTime(self):
        '''
        Returns the current time in string form.
        '''
        return datetime.now().strftime('%Y-%m-%d %H:%M:%S')
        
    def record_status(self, status='started', start_time=None, jresult=None, jinfo=None):
        '''
        Records status of DNA.
        
        Args:
            status: The following values are recommended:
                'started', 'done', 'in progress', 'warning', 'error', 'failed', 'aborted', 'stopped'
                Default: 'started'
            start_time: The 'StartTime' attribute. If the time it took to complete the
                DNA call is desired, then set this argument with the DNA start time.
            jresult: Optional JSON object containing results.
            jinfo: Optional JSON object containing further information describing the status.
        
        Returns:
            Current time
        '''
        now = self.getCurrentTime()
        if self.rpc_context.username != None:
            classname = get_class_name(self)
            temporal_biz = TemporalBiz(self.rpc_context, 'report/dna')
            report = json.loads('{}')
            if classname != None:
                report['Dna'] = classname
            report['Time'] = now
            if start_time != None:
                report['StartTime'] = start_time
            report['Status'] = status
            if jresult != None:
                report['Result'] = jresult
            if jinfo != None:
                report['Info'] = jinfo
            temporal_biz.put(self.rpc_context.username, report)
        return now
        