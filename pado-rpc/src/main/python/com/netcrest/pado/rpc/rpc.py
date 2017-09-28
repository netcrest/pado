
import json
import logging
import sys
from time import sleep

from com.netcrest.pado.rpc.rpc_client import RpcClient
from com.netcrest.pado.rpc.rpc_shared import RpcShared
from com.netcrest.pado.rpc.util import rpc_util


rpc = None

def main(server_id, request):
    '''Launch the specified JSON request.
    
    Args:
        server_id: Unique data node server ID.
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
   
    if 'mqtthost' in request:    
        mqtthost = jrequest['mqtthost']
    else:
        mqtthost = 'localhost'
    if 'mqttport' in request:    
        mqttport = jrequest['mqttport']
    else:
        mqttport = 1883

    rpc = RpcClient(server_id, mqtthost, mqttport)
    
    # rpc is globally shared
    RpcShared.rpc = rpc
    
    rpc_util.process_request(jrequest)
    
    while rpc.is_closed() == False:
        sleep(5)
 
   
# def __thread_invoke(jrequest):
# #     global rpc
#     jresult = invoke(jrequest)
#     return jresult

if __name__ == '__main__':
#     logging.basicConfig(filename='log/rpc_client.log', filemode='w')
#     logger = logging.getLogger('main')

    if len(sys.argv) < 3:
        sys.stderr.write("2 arguments required: <server_id> <request>")
        sys.stderr.flush()
        sys.exit(-1)
    server_id = sys.argv[1]
    request = sys.argv[2]
    main(server_id, request)
    