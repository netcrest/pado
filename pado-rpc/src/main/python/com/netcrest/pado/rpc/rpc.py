import json
import logging
import sys
import threading

from com.netcrest.pado.rpc.rpc_client import RpcClient
from com.netcrest.pado.rpc.rpc_shared import RpcShared
from com.netcrest.pado.rpc.util.rpc_util import invoke, create_reply

rpc = None

def main(request):
    '''Launch the specified JSON request.
    
    Args:
        request: Pado extension of JSON RPC 2.0 request with the following parameters:
        
        Required Parameters:
            "id": Unique request id
            "classname": name of the class to instantiate
            "method": Name of the method in the class to invoke
            "params": JSON object with the required method parameters
        Optional Parameters:
            "jsonrpc": Always "2.0"
            "lang": Always "python"
    
    Returns:
        N/A
        
    Example:
        request = "{
            "id": "184787637706666",
            "classname": "com.jnj.ai.rpc.biz.query_demo.QueryDemo",
            "method": "execute_pql",
            "params": {
                "fetchSize": 1000,
                "pql": "company/sales?CustomerId:joe"
            },
            "jsonrpc": "2.0",
            "lang": "python"
        }"
    
    '''
    logger = logging.getLogger('main')
    jrequest = json.loads(request)
    is_daemon = True
    if 'daemon' in jrequest:
        is_daemon = jrequest['daemon']
    print('is_daemon=' + str(is_daemon))
    if 'mqtthost' in request:    
        mqtthost = jrequest['mqtthost']
    else:
        mqtthost = 'localhost'
    if 'mqttport' in request:    
        mqttport = jrequest['mqttport']
    else:
        mqttport = 1883
    global rpc
    rpc = RpcClient(mqtthost, mqttport)
    
    # rpc is globally shared
    RpcShared.rpc = rpc
    
    if is_daemon:
        jresult = invoke(jrequest)
        if 'error' in jresult:
            error = jresult['__error']
            jreply = create_reply(jrequest)
            jreply['error'] = error
        else:
            jreply = create_reply(jrequest, jresult)
        
        rpc.send_result(jreply)
        try:
            rpc.close()
        except:
            print('Unexpected error: ' + str(sys.exc_info()[0]))
#         sys.exit()
    else:
        try:
            jreply = create_reply(jrequest)
            rpc.send_result(jreply)
            thread = WokerThread(rpc, jrequest)
            thread.start()
            thread.join()
            jresult = thread.jresult
            jreply = create_reply(jrequest, jresult)
            try:
                rpc.close()
            except:
                print('Unexpected error: ' + str(sys.exc_info()[0]))
#             sys.exit()
        except:
            print('Unexpected error: ' + str(sys.exc_info()))
 
   
def __thread_invoke(jrequest):
#     global rpc
    jresult = invoke(jrequest)
    return jresult


class WokerThread(threading.Thread):
    rpc = None
    jrequest = None
    jresult = None
    def __init__(self, rpc, jrequest):
        threading.Thread.__init__(self, name='RpcWorkerThread')
        self.rpc = rpc
        self.jrequest = jrequest
    
    def run(self):
        jresult = invoke(self.jrequest)
    
    def get_result(self):
        return self.jresult
 
if __name__ == '__main__':
#     logging.basicConfig(filename='log/rpc_client.log', filemode='w')
    logger = logging.getLogger('main')
    if len(sys.argv) == 1:
        logger.info("The argument request in JSON form must be supplied")
        sys.exit(-1)
    request = sys.argv[1];
    print('request=' + request)
    main(request)
    
    
    