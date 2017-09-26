'''
Created on Sep 18, 2017

@author: dpark
'''
# from com.netcrest.pado.rpc.rpc_client import RpcClient


class RpcShared(object):
    '''
    RpcShared contains globally shared parameters accessible by inheritance or direct access.
    
    It is recommended all classes that need to interact with the data node should inherit 
    this class to obtain the shared parameters.
    
    Shared Parameters:
        rpc: RpcClient instance that maintains a connection to the data node and provides methods send requests and the final result. 
    '''
    rpc = None
    
    def __init__(self):
        pass