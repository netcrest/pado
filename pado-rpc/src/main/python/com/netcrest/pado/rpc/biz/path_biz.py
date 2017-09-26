'''
Created on Sep 14, 2017

@author: dpark
'''
import json

from com.netcrest.pado.rpc.rpc_shared import RpcShared
from com.netcrest.pado.rpc.util.rpc_util import create_request


class PathBiz(RpcShared):
    '''
    PathBiz provides grid path methods
    '''
    _grid_path = None
    _biz_class_name = 'com.netcrest.pado.rpc.client.biz.PathRpcBiz';
    
    def __init__(self, grid_path):
        '''
        Constructs a new instance of PathBiz.
        
        Args:
            grid_path: Grid path (not full path)
        '''
        self._grid_path = grid_path
        
    def query(self, queryPredicate):
        '''Execute the specified query predicate on the grid path.
        
        Args:
            queryPredicate: Query predicate is a where clause with out the select projection.
            
        Returns: Query results in JSON RPC reply form
        
        Example:
            value['Name']='Smith'
        '''
        jparams = json.loads('{}')
        jparams['gridPath'] = self._grid_path
        jparams['queryPredicate'] = queryPredicate
        jrequest = create_request(self._biz_class_name, 'query', jparams)
        return self.rpc.execute(jrequest, 10)
    
    def dump(self):
        '''Dump the grid path contents in the default data node dump directory.
        
        Returns: Execution results in JSON.
        '''
        jparams = json.loads('{}')
        jparams['gridPath'] = self._grid_path
        jrequest = create_request(self._biz_class_name, 'dumpGridPath', jparams)
        return self.rpc.execute(jrequest, 10)
    
    def size(self):
        '''Get the size of the grid path in the data node.
        
        Returns: Size in JSON RPC reply form.
        '''
        jparams = json.loads('{}')
        jparams['gridPath'] = self._grid_path
        jrequest = create_request(self._biz_class_name, 'size', jparams)
        return self.rpc.execute(jrequest, 10)
    
    def addListener(self, listener_name, listener):
        '''Add a listener to listen on data changes made in the grid path
        
        Args:
            listener_name: Unique name of the listener.
            listener: Listener function or class method with one parameter for receiving JSON messages.
            
        Returns:
            Addition results in JSON RPC reply form.
            
        Example:
            path_biz = PathBiz('company/sales')
            path_biz.addListener('some_grid_path', some_grid_path_rpc_listener)
            def some_grid_path_rpc_listener(message):
                print('message=' + str(message))
        '''
        jparams = json.loads('{}')
        jparams['gridPath'] = self._grid_path
        jparams['name'] = listener_name
        jrequest = create_request(self._biz_class_name, 'addListener', jparams)
        self.rpc.add_rpc_listener(listener_name, listener)
        return self.rpc.execute(jrequest, 10)
    
    def removeListener(self, listener_name, listener):
        '''Remove the specified listener
        
        Args:
            listener_name: Unique name of the listener.
            listener: previously added Listener function or class method.
         
        Returns:
            Removal results in JSON RPC reply form.
            
        Example:
            path_biz = PathBiz('company/sales')
            path_biz.addListener('some_grid_path', some_grid_path_rpc_listener)
            path_biz.removeListener('some_grid_path', some_grid_path_rpc_listener)
            
            def some_grid_path_rpc_listener(message):
                print('message=' + str(message))
        '''
        jparams = json.loads('{}')
        jparams['gridPath'] = self._grid_path
        jparams['name'] = listener_name
        jrequest = create_request(self._biz_class_name, 'removeListener', jparams)
        self.rpc.remove_rpc_listener(listener_name, listener)
        return self.rpc.execute(jrequest, 10)