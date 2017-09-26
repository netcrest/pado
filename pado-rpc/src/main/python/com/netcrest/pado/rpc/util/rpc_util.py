'''
Created on Sep 14, 2017

@author: dpark
'''
import importlib
import json
import time

def create_request(class_name, method_name, params):
    request = json.loads('{"jsonrpc":"2.0"}')
    request['id'] = str(time.time())
    if class_name != None:
        request['classname'] = class_name
    if method_name != None:
        request['method'] = method_name
    if params != None:
        request['params'] = params
    return request

def create_reply(jrequest, jresult = None, error_code = None, error_message = None, error_data = None):
    jreply = json.loads('{"jsonrpc":"2.0"}')
    jreply['id'] = jrequest['id']
    if jresult != None:
        jreply['result'] = jresult
    if error_code != None:
        error = json.loads('{}')
        error['code'] = error_code
        if error_message != None:
            error['message'] = error_message
        if error_data != None:
            error['data'] = error_data
        jreply['error'] = error
    return jreply

def create_error_wrap(jerror = None, error_code = -1, error_message = None, error_data = None):
   
    if jerror == None:
        jerror = json.loads('{}')
        jerror['code'] = error_code
        if error_message != None:
            jerror['message'] = error_message
        if error_data != None:
            jerror['data'] = error_data
    retval = json.loads('{}')
    retval['__error'] = jerror
    return retval
 
def invoke(jrequest):
    if jrequest == None:
        return None    
    class_name = jrequest['classname']
    method_name = jrequest['method']
    jparams = jrequest['params']
    index = class_name.rfind('.')
    module_name = class_name[0:index]
    class_name = class_name[index+1:]
    module = importlib.import_module(module_name)
    clazz = getattr(module, class_name)
    obj = clazz()
    jresult = getattr(obj, method_name)(jparams)
    return jresult
