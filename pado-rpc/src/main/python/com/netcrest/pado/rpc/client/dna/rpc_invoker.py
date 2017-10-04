'''
Created on Oct 4, 2017

@author: dpark
'''
from com.netcrest.pado.rpc.rpc_shared import RpcShared
from com.netcrest.pado.rpc.util import rpc_util
from com.netcrest.pado.rpc.util.rpc_util import create_request


class RpcInvoker(RpcShared):
    '''
    RpcInvoker invokes IRpc methods.
    
    RpcInvoker directly invokes IRpc implementation class methods.
    Typically, a biz wrapper class that hides the RPC details is used to invoke
    IRpc objects, instead. Although this class can directly be used by
    applications, it is more appropriate for remotely testing IRpc
    classes.
    '''
    
    def __init__(self):
        pass
        
    def invoke(self, jparams):
        print('rpc_invoker.invoker() entered')
        if not 'classname' in jparams:
            return None
        rpc_class_name = jparams['classname']
        if not 'method' in jparams:
            return None 
        method = jparams['method']
        if not 'method' in jparams:
            rpc_params = None
        else:
            rpc_params = jparams['params']
        if not 'timeout' in jparams:
            timeout = 10000
        else:
            timeout = jparams['timeout']
        timeout_in_sec = timeout / 1000
        
        print('rpc_invoker.invoker(): timeout=' + str(timeout_in_sec))
        jrequest = create_request(rpc_class_name, method, rpc_params)
        print('rpc_invoker.invoker(): jrequest=' + str(jrequest))
        jresult = self.rpc.execute(jrequest, timeout_in_sec)
        if 'error' in jresult:
            error = jresult['error']
            return rpc_util.create_error_wrap(jerror=error)
        else:
            if 'result' in jresult:
                return jresult['result']
            else:
                return None
        
        
