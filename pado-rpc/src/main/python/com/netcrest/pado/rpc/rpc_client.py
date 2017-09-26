'''
Created on Sep 14, 2017

@author: dpark
'''
import json
import logging
import platform
import sys
import threading
import uuid

from com.netcrest.pado.rpc.singleton import Singleton
from paho.mqtt.client import MQTT_ERR_SUCCESS
import paho.mqtt.client as mqtt

class RpcClient(Singleton):
    '''
    RpcClient establishes RPC services via MQTT.
    
    RpcClient connects to the MQTT broker and subscribes its "reply"
    topic. It also provides methods to execute "request" services on the data
    node and the "result" method to send the result to the data node that
    originated the start of the client app which led to use of this class.
    '''
#     __metaclass__=Singleton
    topic_request = '/__pado/request'
    topic_result = '/__pado/result'
    topic_reply = '/__pado/reply/' + uuid.uuid1().hex
    thread = None
    is_terminated = False
    id_map = dict()
    id_rpc_listener_map = dict()
    live_id_rpc_listener_map = dict()
    client = None
    host = 'localhost'
    port = 1883
    logger = logging.getLogger('RpcClient')
    
    def __init__(self, host='localhost', port=1883):
        '''
        Constructor
        '''
        print("params=", host, port)
        self.host = host
        self.port = port
        self.client = mqtt.Client()
        self.client.on_connect = self.__on_connect
        self.client.on_message = self.__on_message
        self.client.on_publish = self.__on_publish
        self.client.connect(host, port, 600)
        self.client.subscribe(self.topic_reply, 2)
        self.thread = threading.Thread(target=self.__run)
        self.thread.setDaemon(True)
        self.thread.start()
        print('=============== RpcClient.__init__(): host=' + host + ", port=", port)
        
        
    # The callback for when the client receives a CONNACK response from the server.
    def __on_connect(self, client, userdata, flags, rc):
        print("RpcClient.__on_connect(): Connected with result code " + str(rc))
    
    # The callback for when a PUBLISH message is received from the server.
    def __on_message(self, client, userdata, msg):
        # A bug in Paho MQTT on Windows. The payload contains extra characters
        if platform.system() == 'Windows':
            reply = str(msg.payload)[2:-1]
        else:
            reply = str(msg.payload)
        self.logger.info("RpcClient.__on_message().reply: " + msg.topic + " " + reply)
        jreply = json.loads(reply)
        self.logger.info("RpcClient.__on_message().jreply: " + msg.topic + " ", jreply)
        if 'id' in jreply:
            id_ = jreply['id']
            if id_ in self.id_map:
                threadReply = self.id_map[id_]
                if threadReply != None:
                    threadReply.__condition__.acquire()
                    threadReply.jreply = jreply
                    threadReply.__condition__.notify()
                    threadReply.__condition__.release()
        else:
            print('rpc_client.__on_message() - msg.topic=' + msg.topic)
            live_listener_set = self.getLiveRpcListenerSet(msg.topic)
            if live_listener_set != None:
                for listener in live_listener_set:
                    listener(reply)
    
    def __on_publish(self, client, userdata, mid):
        print("RpcClient.__on_publish(): mid=", mid)
            
    def __run(self):
        while (self.is_terminated == False):
            self.client.loop(1.0)
    
    def close(self):
        self.is_terminated = True
        try:
            self.client.disconnect()
        except:
            print('Unexpected error: ' + str(sys.exc_info()[0]))
    
    # Returns JSON reply
    def execute(self, jrequest, timeout = 0):
        '''Execute the specified request
        
        Args:
            jrequest: JSON RPC 2.0 request
            timeout: Timeout in sec. Default is 0, i.e., no timeout
            
        Returns:
            JSON RPC 2.0 reply
        '''
        jreply = None
        threadReply = None
        id_ = jrequest['id']
        if id_ != None:
            threadReply = self.ThreadReply(threading.currentThread())
            self.id_map[id_] = threadReply
            jrequest['replytopic'] = self.topic_reply
            request = json.dumps(jrequest)
            (result, mid) = self.client.publish(self.topic_request, request, 2, False)
            if result == MQTT_ERR_SUCCESS:
                print('rpc_client.execute() - publish success')
                
            if timeout < 0:
                timeout = 0
            threadReply.__condition__.acquire()
            while threadReply.jreply == None:
                threadReply.__condition__.wait(timeout)
            threadReply.__condition__.release()
            jreply = threadReply.jreply    
        return jreply
    
    def __subscribe(self, topic):
        self.client.subscribe(topic, 2)
        
    def __unsubscribe(self, topic):
        self.client.unsubscribe(topic)
        
    def send_result(self, jresult):
        result = json.dumps(jresult)
        info = self.client.publish(self.topic_result, result, 2, False)
        info.wait_for_publish()
       
    def add_rpc_listener(self, listener_name, listener):
        if listener_name == None or len(listener_name) == 0 or listener == None:
            return
        topic = '/__pado/listener/' + listener_name
        if topic in self.id_rpc_listener_map:
            listener_set = self.id_rpc_listener_map[topic]
            if topic in listener_set:
                return
        else:
            listener_set = set()
            self.id_rpc_listener_map[topic] = listener_set
        self.__subscribe(topic)
        listener_set.add(listener)
        self.live_id_rpc_listener_map[topic] = set(listener_set)
        
    def remove_rpc_listener(self, listener_name, listener):
        if listener_name == None or len(listener_name) == 0 or listener == None:
            return
        topic = '/__pado/listener/' + listener_name
        if topic in self.id_rpc_listener_map:
            listener_set = self.id_rpc_listener_map[topic]
            if len(listener_set) == 0:
                self.__unsubscribe(topic)
            listener_set.remove(listener)
            self.live_id_rpc_listener_map[topic] = set(listener_set)
       
    def getLiveRpcListenerSet(self, topic):
        if topic == None or len(topic) == 0:
            return None
        if topic in self.live_id_rpc_listener_map:
            return self.live_id_rpc_listener_map[topic]
        else:
            return None
         
    class ThreadReply:
        thread = None
        jreply = None
        __condition__ = threading.Condition()
        def __init__(self, thread):
            self.thread = thread
    
            
