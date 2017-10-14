'''
Created on Oct 4, 2017

@author: dpark
'''
from com.netcrest.pado.rpc.rpc_shared import RpcShared
from com.netcrest.pado.rpc.util import rpc_util
from com.netcrest.pado.rpc.util.rpc_util import create_request


class RpcInvokerDna(RpcShared):
    '''
    RpcInvokerDna invokes IRpc methods.
    
    RpcInvokerDna directly invokes IRpc implementation class methods.
    Typically, a biz wrapper class that hides the RPC details is used to invoke
    IRpc objects, instead. Although this class can directly be used by
    applications, it is more appropriate for remotely testing IRpc
    classes.
    '''
    
    def __init__(self):
        pass
        
    def invoke(self, jparams):
        print('rpc_invoker_dna.invoke() entered')
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
        
        print('rpc_invoker_dna.invoke(): timeout=' + str(timeout))
        jrequest = create_request(rpc_class_name, method, rpc_params)
        print('rpc_invoker_dna.invoke(): jrequest=' + str(jrequest))
        jresult = self.rpc.execute(self.rpc_context, jrequest, timeout)
        if 'error' in jresult:
            error = jresult['error']
            return rpc_util.create_error_wrap(jerror=error)
        else:
            if 'result' in jresult:
                return jresult['result']
            else:
                return None