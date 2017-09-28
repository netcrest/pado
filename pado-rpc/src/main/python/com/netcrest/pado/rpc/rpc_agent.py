import sys
from time import sleep

from com.netcrest.pado.rpc.rpc_client import RpcClient
from com.netcrest.pado.rpc.rpc_shared import RpcShared


def main(server_id):
    '''Launch rpc agent.
    
    Args:
        server_id: Unique data node server ID.
        
    Returns:
        N/A
    '''   
    mqtthost = 'localhost'
    mqttport = 1883

    rpc = RpcClient(server_id, mqtthost, mqttport, True)
    
    # rpc is globally shared
    RpcShared.rpc = rpc
    
    while rpc.is_closed() == False:
        sleep(10)
 
   
# def __thread_invoke(jrequest):
# #     global rpc
#     jresult = invoke(jrequest)
#     return jresult

if __name__ == '__main__':
#     logging.basicConfig(filename='log/rpc_client.log', filemode='w')
#     logger = logging.getLogger('main')
    if len(sys.argv) < 2:
        sys.stderr.write("q argument required: <server_id>")
        sys.stderr.flush()
        sys.exit(-1)
    server_id = sys.argv[1]
    main(server_id)
    