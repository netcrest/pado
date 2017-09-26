'''
Created on Sep 14, 2017

@author: dpark
'''
import json

from com.netcrest.pado.rpc.rpc_shared import RpcShared
from com.netcrest.pado.rpc.util.rpc_util import create_request


class QueryBiz(RpcShared):
    '''
    QueryBiz provides methods to execute queries on the data node.
    '''
    
    _biz_class_name = 'com.netcrest.pado.rpc.client.biz.QueryBiz';
    
    def __init__(self):
        pass
            
    def execute_pql(self, pql, fetch_size=None):
        '''Execute the specified PQL (Pado Query Language.
        
        Args:
            pql: Pado query statement
            fetch_size: Fetch size per page. This is an optional parameter. The default value is configured by the server.
            
        Returns:
            Query results in JSON RPC reply form.
        '''
        jparams = json.loads('{}')
        jparams['pql'] = pql
        if fetch_size != None:
            jparams['fetchSize'] = fetch_size
        jrequest = create_request(self._biz_class_name, 'executePql', jparams)
        return self.rpc.execute(jrequest, 10)
    
    def next_result_set(self, jresult):
        '''Return the next result set of the query result.
        
        Args:
            jresult: The returned result of execute_pql() or this method.
    
        Returns:
            None if the end of the result is reached.
        '''
        if jresult == None:
            return None
        if 'result' not in jresult:
            return None
        result = jresult['result']
        if 'pql' not in result:
            return None
        else:
            pql = result['pql']
        if 'nextBatchIndexOnServer' not in result:
            nextBatchIndexOnServer = -1
        else:
            nextBatchIndexOnServer = result['nextBatchIndexOnServer']
        if nextBatchIndexOnServer < 0:
            return None
        if 'totalSizeOnServer' not in result:
            totalSizeOnServer = -1
        else:
            totalSizeOnServer = result['totalSizeOnServer']
        if nextBatchIndexOnServer >= totalSizeOnServer:
            return None
        
        startIndex = nextBatchIndexOnServer   
        jparams = json.loads('{}')
        jparams['pql'] = pql
        jparams['startIndex'] = startIndex
        if 'fetchSize' in result:
            fetchSize = result['fetchSize']
            jparams['fetchSize'] = fetchSize
        jrequest2 = create_request(self._biz_class_name, 'executePql', jparams)
        return self.rpc.execute(jrequest2, 10)
        
        
        
