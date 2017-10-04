'''
Created on Sep 14, 2017

@author: dpark
'''
import json

from com.netcrest.pado.rpc.rpc_shared import RpcShared
from com.netcrest.pado.rpc.util.rpc_util import create_request


class TemporalBiz(RpcShared):
    '''
    TemporalBiz provides methods to access temporal data.
    '''
    _grid_path = None
    _biz_class_name = 'com.netcrest.pado.rpc.client.biz.TemporalBiz';
    
    def __init__(self, grid_path):
        '''
        Constructs a new instance of PathBiz.
        
        Args:
            grid_path: Grid path (not full path)
        '''
        self._grid_path = grid_path
        
    def get(self, identity_key, valid_at_time=-1, as_of_time=-1):
        '''Returns the latest value as of now mapped by the specified identity key.
        
        Args:
            identity_key: Temporal identity key
            
        Returns: 
            Latest value of the specified identity key. None if not found.
        '''
        jparams = json.loads('{}')
        jparams['gridPath'] = self._grid_path
        jparams['identityKey'] = identity_key
        jparams['validAtTime'] = valid_at_time
        jparams['asOfTime'] = as_of_time
        jrequest = create_request(self._biz_class_name, 'get', jparams)
        return self.rpc.execute(jrequest, 10)
    
    def get_entry(self, identity_key, valid_at_time=-1, as_of_time=-1):
        '''Returns the latest temporal entry as of now mapped by the specified identity key.
        
        Args:
            identity_key: Temporal identity key.
            valid_at_time: Valid at time in msec.
            as_of_time: As of time in msec.
            
        Returns: 
            Latest entry of the specified identity key. None if not found.
        '''
        jparams = json.loads('{}')
        jparams['gridPath'] = self._grid_path
        jparams['identityKey'] = identity_key
        jparams['validAtTime'] = valid_at_time
        jparams['asOfTime'] = as_of_time
        jrequest = create_request(self._biz_class_name, 'getEntry', jparams)
        return self.rpc.execute(jrequest, 10)
    
    def get_all_entries(self, identity_key, valid_at_time=-1, as_of_time=-1):
        '''Returns all temporal entries of the specified identity key as of now.
        
        Args:
            identity_key: Temporal identity key.
            valid_at_time: Valid at time in msec.
            as_of_time: As of time in msec.
            
        Returns: 
            A chronologically ordered set providing a history of changes that
            fall in the valid-at time. The entries are ordered by start-valid and
            written times.
        '''
        jparams = json.loads('{}')
        jparams['gridPath'] = self._grid_path
        jparams['identityKey'] = identity_key
        jparams['validAtTime'] = valid_at_time
        jparams['asOfTime'] = as_of_time
        jrequest = create_request(self._biz_class_name, 'getAllEntries', jparams)
        return self.rpc.execute(jrequest, 10)
    
    def get_entry_history_written_time_range_list(self, pql, valid_at_time, from_written_time, to_written_time):
        '''Returns the temporal entries that satisfy the specified valid-at and end written time range for the given PQL query string. 
     
            Note that this method may retrieve one or more valid objects per temporal list.
            It searches temporal values that fall in the specified written time range.
        
        Args:
            pql: Pado query statement
            identity_key: Temporal identity key.
            valid_at_time: Valid at time in msec.
            from_written_time: Start of the written time range. -1 for current time.
            to_written_time: End of the written time range. -1 for current time.
            
        Returns: 
            Array of temporal entries in JSON key/value pairs.
        '''
        jparams = json.loads('{}')
        jparams['queryStatement'] = pql
        jparams['validAtTime'] = valid_at_time
        jparams['fromWrittenTime'] = from_written_time
        jparams['toWrittenTime'] = to_written_time
        jrequest = create_request(self._biz_class_name, 'getEntryHistoryWrittenTimeRangeList', jparams)
        return self.rpc.execute(jrequest, 10)
    
    def size(self, valid_at_time = -1, as_of_time = -1):
        '''Returns the total count of entries that satisfy the specified valid-at and as-of times.
        
        Args:
            valid_at_time: The time at which the value is valid. If -1, then current time.
            as_of_time: The as-of time compared against the written times. If -1, then current time.
            
        Returns: Size in integer.
        '''
        jparams = json.loads('{}')
        jparams['gridPath'] = self._grid_path
        jparams['validAtTime'] = valid_at_time
        jparams['asOfTime'] = as_of_time
        jrequest = create_request(self._biz_class_name, 'size', jparams)
        return self.rpc.execute(jrequest, 10)
    
    def get_temporal_list_count(self):
        '''Returns the total count of temporal lists. The returned number represents the total number of unique identity keys.
        
        Args:
            N/A
            
        Returns: 
            Count in integer.
        '''
        jparams = json.loads('{}')
        jparams['gridPath'] = self._grid_path
        jrequest = create_request(self._biz_class_name, 'getTemporalListCount', jparams)
        return self.rpc.execute(jrequest, 10)
