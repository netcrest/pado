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
    _biz_class_name = 'com.netcrest.pado.rpc.client.biz.PathBiz';
    
    def __init__(self, rpc_context, grid_path):
        '''
        Constructs a new instance of PathBiz.
        
        Args:
            rpc_context: RPC context object
            grid_path: Grid path (not full path)
        '''
        self.rpc_context = rpc_context
        self._grid_path = grid_path
        
    def put(self, key, value):
        '''Puts the specified (key, value) pair in the grid path.
        
        Args:
            key: Key of string or numeric type.
            value: Value of string, numeric or JSON object (JSON array not supported)
            
        Returns: Value put in the grid path
        '''
        jparams = json.loads('{}')
        jparams['gridPath'] = self._grid_path
        jparams['key'] = key
        jparams['value'] = value
        jrequest = create_request(self._biz_class_name, 'put', jparams)
        return self.rpc.execute(self.rpc_context, jrequest, 0)
    
    def put_all(self, entry_map):
        '''Puts the entries on the specified map the grid path.
        
        Args:
            entry_map: (key, value) entries. Must be of JSON type.
            
        Returns: void
        '''
        jparams = json.loads('{}')
        jparams['gridPath'] = self._grid_path
        jparams['entryMap'] = entry_map
        jrequest = create_request(self._biz_class_name, 'putAll', jparams)
        return self.rpc.execute(self.rpc_context, jrequest, 0)
    
    def remove(self, key):
        '''Removes the specified key from the grid path.
        
        Args:
            key: Key to remove. Must be of string or numeric type.
            
        Returns: Removed value.
        '''
        jparams = json.loads('{}')
        jparams['gridPath'] = self._grid_path
        jparams['key'] = key
        jrequest = create_request(self._biz_class_name, 'remove', jparams)
        return self.rpc.execute(self.rpc_context, jrequest, 0)
    
    def get(self, key):
        '''Gets the value of the specified key from the grid path.
        
        Args:
            key: Key of string or numeric type.
            
        Returns: Key mapped value in the grid path.
        '''
        jparams = json.loads('{}')
        jparams['gridPath'] = self._grid_path
        jparams['key'] = key
        jrequest = create_request(self._biz_class_name, 'get', jparams)
        return self.rpc.execute(self.rpc_context, jrequest, 0)
    
    def get_all(self, key_array):
        '''Gets the values of the specified keys from the grid path.
        
        Args:
            key: JSON array of keys. Keys must be of string or numeric type.
            
        Returns: JSON object containing (key, value) pairs.
        '''
        jparams = json.loads('{}')
        jparams['gridPath'] = self._grid_path
        jparams['keyArray'] = key_array
        jrequest = create_request(self._biz_class_name, 'getAll', jparams)
        return self.rpc.execute(self.rpc_context, jrequest, 0)
    
    def query(self, queryPredicate):
        '''Execute the specified query predicate on the grid path.
        
        Args:
            queryPredicate: Query predicate is a where clause with out the select projection.
            
        Returns: Query results in JSON RPC reply form
        
        Example:
            query("value['Name']='Smith'")
        '''
        jparams = json.loads('{}')
        jparams['gridPath'] = self._grid_path
        jparams['queryPredicate'] = queryPredicate
        jrequest = create_request(self._biz_class_name, 'query', jparams)
        return self.rpc.execute(self.rpc_context, jrequest, 0)
    
    def dump_grid_path(self):
        '''Dump the grid path contents in the default data node dump directory.
        
        Returns: Execution results in JSON.
        '''
        jparams = json.loads('{}')
        jparams['gridPath'] = self._grid_path
        jrequest = create_request(self._biz_class_name, 'dumpGridPath', jparams)
        return self.rpc.execute(self.rpc_context, jrequest, 0)
    
    def size(self):
        '''Gets the size of the grid path in the data node.
        
        Returns: Size in JSON RPC reply form.
        '''
        jparams = json.loads('{}')
        jparams['gridPath'] = self._grid_path
        jrequest = create_request(self._biz_class_name, 'size', jparams)
        return self.rpc.execute(self.rpc_context, jrequest, 0)
    
    def add_listener(self, listener_name, listener):
        '''Adds a listener to listen on data changes made in the grid path
        
        Args:
            listener_name: Unique name of the listener.
            listener: Listener function or class method with one parameter for receiving JSON messages.
            
        Returns:
            Addition results in JSON RPC reply form.
            
        Example:
            path_biz = PathBiz(rpc_context, 'company/sales')
            path_biz.addListener('some_grid_path', some_grid_path_rpc_listener)
            def some_grid_path_rpc_listener(message):
                print('message=' + str(message))
        '''
        jparams = json.loads('{}')
        jparams['gridPath'] = self._grid_path
        jparams['name'] = listener_name
        jrequest = create_request(self._biz_class_name, 'addListener', jparams)
        self.rpc.add_rpc_listener(listener_name, listener)
        return self.rpc.execute(self.rpc_context, jrequest, 0)
    
    def remove_listener(self, listener_name, listener):
        '''Remove the specified listener
        
        Args:
            listener_name: Unique name of the listener.
            listener: Previously added Listener function or class method.
         
        Returns:
            Removal results in JSON RPC reply form.
            
        Example:
            path_biz = PathBiz(rpc_context, 'company/sales')
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
        return self.rpc.execute(self.rpc_context, jrequest, 0)