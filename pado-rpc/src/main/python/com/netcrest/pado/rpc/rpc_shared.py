'''
Created on Sep 18, 2017

@author: dpark
'''

class RpcContext(object):
    '''
    RpcContext provide RPC session information.
    
    Paramenters:
        token: User session token received from the request sent by the data node.
        username: User name.
    '''
    token = None
    username = None
    
    def __init__(self, token=None, username=None):
        '''
        Constructs a new RpcContext object.
        '''
        self.token = token
        self.username = username
        
class RpcShared(object):
    '''
    RpcShared contains globally shared parameters accessible by inheritance or direct access.
    
    It is recommended that all classes that need to interact with the data node should inherit 
    this class to obtain the shared parameters.
    
    Shared Parameters:
        rpc: Static RpcClient instance that maintains a connection to the data node and
            provides methods send requests and the final result. 
        rpc_context: Non-static RpcContext that provides session information required
            to communicate with the data node.
    '''
    rpc = None
    
    rpc_context = RpcContext()
    
    def __init__(self):
        pass