import json
import uuid

import requests
from _ast import arg

class Pado:
    '''
    Pado connects to a Pado grid and provides REST services for accessing IBiz objects.
    '''
    url = 'localhost:8080'
    token = None
    gid = None
    username = None
    
    def __init__(self, url):
        '''Constructs a new instance of PathBiz.
        
        Args:
            url: Pado-web URL of the format, http(s)://host:port, e.g., http://localhost:8080. 
        '''
        self.url = url
        
    def login(self, appid, username, passwd=None):
        '''Logs in to the Pado grid. 
        
        Args:
            appid: Application ID, e.g., "sys", "test".
            username: User name
            passwd: Password Optional if the Pado grid has not been configured for authentication.
            
        Returns:
            The response from the REST call in the form of JSON object.
        '''
        url = self.url + '/pado-web/pado?method=login&username=' + username + '&appid=' + appid
        if passwd != None:
            url += '&password=' + passwd
        response = requests.post(url, headers={"Content-Type": "application/json"})
        jresponse = response.json()
        if 'token' in jresponse:
            self.token = jresponse['token']
        if 'gid' in jresponse:
            self.gid = jresponse['gid']
        self.username = username
        return jresponse
    
    def catalog(self, filter=None):
        '''Returns the IBiz catalog information. The IBiz catalog contains IBiz objects that
        you can remotely invoke. 
        
        Args:
            filter: Regular expression to filter the list of IBiz objects in the catalog.
            
        Returns:
            The response from the REST call in the form of JSON object.
        '''
        url = self.url + '/pado-web/pado?token=' + self.token + "&method=catalog"
        if filter != None:
            url += '&filter=' + filter
        response = requests.post(url, headers={"Content-Type": "application/json"})
        return response.json()
        
    def logout(self):
        '''
        Logs out from the Pado grid.
        
        Returns:
            The response from the REST call in the form of JSON object.
        '''
        url = self.url + '/pado-web/pado?token=' + self.token + "&method=logout"
        response = requests.post(url, headers={"Content-Type": "application/json"})
        return response.json()
    
    def invoke(self, ibiz, method, jparams):
        '''Invokes the specified IBiz class' method with the specified parameters.
        
        Args:
            ibiz: Fully-qualified IBiz class name. See the available IBiz classes by
                invoking the catalog() method.
            method: Method name
            jparams: Method parameters in the form of JSON object.
            
        Returns:
            The response from the REST call in the form of JSON object.
        '''
        url = self.url + '/pado-web/pado?token=' + self.token + '&ibiz=' + ibiz + '&method=' + method + '&args=' + json.dumps(jparams)
        response = requests.post(url, headers={"Content-Type": "application/json"})
        return response.json()
    
    def invoke_vargs(self, ibiz, cargs, method, *args):
        '''Invokes the specified IBiz class' method with the specified parameters.
        
        Args:
            ibiz: Fully-qualified IBiz class name. See the available IBiz classes by
                invoking the catalog() method.
            cargs: Constructor argument list. Must of array or list type. None if
                the constructor takes no arguments.
            method: Method name
            args: Variable argument list, i.e., natural function argument list to
                the method in sequential order.    
            
        Returns:
            The response from the REST call in the form of JSON object.
        '''
        arg_list = ''
        for arg in args:
            arg_list += '&args=' + str(arg)
        url = self.url + '/pado-web/pado?token=' + self.token + '&ibiz=' + ibiz + '&method=' + method
        if arg_list != '':
            url += arg_list
        if cargs != None:
            carg_list = ''
            for carg in cargs:
                carg_list += '&cargs=' + str(carg)
                url += carg_list
        response = requests.post(url, headers={"Content-Type": "application/json"})
        return response.json()
    
    def query(self, query_string, validat=None, asof=None, batch=None, ascend=True, orderby=None, refresh=False, cursor='next'):
        '''Executes the specified query statement in the grid.
        
        Args:
            query_string: PQL query statement
            validat: Valid-at time in date format "yyyyMMddHHmmssSSS"
            asof: As-of time in date format of "yyyyMMddHHmmssSSS"
            batch: Streamed batch size. Default: None (or 100)
            ascend: Ascending order flag. true to ascend, false to descend. Default: True
            orderby: Order-by field name
            refresh: True to refresh L2 result set. Default: False
            cursor: "next" to go to the next page (batch) of the result set. 
                "prev" to go the previous page,
                a numeric value to move the cursor to the result index position.
                If the result set is not available then the query is executed and 
                the cursor is positioned at beginning of the result set.
        
        Returns:
            ScrollableResultSet with response that contains results or error.
        '''
        url = self.url + "/pado-web/pado?token=" + self.token + "&query=" + query_string
        if validat != None:
            url += '&validat=' + validat
        if asof != None:
            url += '&asof=' + asof
        if batch != None:
            url += '&batch=' + str(batch)
        if ascend == False:
            url += '&ascend=false'
        if orderby != None:
            url += '&orderby=' + orderby
        if refresh:
            url += '&refresh=true'
        if cursor == None:
            cursor = 'next'
        url += '&cursor=' + cursor
        response = requests.post(url, headers={"Content-Type": "application/json"})
        return ScrollableResultSet(self, query_string, response.json())
 
class ScrollableResultSet:
    '''
    ScrollableResultSet is provides methods to scroll the result set obtained by executing Pado.query().
    '''
    
    def __init__(self, pado, query_string, response):
        '''Constructs a new ScrollableResultSet object with the specified response.
        
        Args:
            pado: Pado instance
            query_string: Query statement used to get the reponse.
            response: Response from executing the specified query string.
        '''
        self._pado = pado
        self._query_string = query_string
        self._jresponse = response
        
    def __query(self, query_string, batch=None, cursor='next'):
        '''Executes the specified query statement in the grid.
        
        Args:
            query_string: PQL query statement
            batch: Streamed batch size. Default: None (or 100)
            cursor: "next" to go to the next page (batch) of the result set. 
                "prev" to go the previous page,
                a numeric value to move the cursor to the result index position.
                If the result set is not available then the query is executed and 
                the cursor is positioned at beginning of the result set.
        
        Returns:
            Query response that contains results or error.
        '''
        url = self._pado.url + "/pado-web/pado?token=" + self._pado.token + "&query=" + query_string
        if batch != None:
            url += '&batch=' + str(batch)
        if cursor == None:
            cursor = 'next'
        url += '&cursor=' + cursor
        response = requests.post(url, headers={"Content-Type": "application/json"})
        return response.json()
    
    def next(self, batch=None):
        '''Returns the next batch of results.
        
        Args:
            batch: Streamed batch size. Default: None (or 100)
        '''
        self._jresponse = None
        self._jresponse = self.__query(self._query_string, batch=batch)
        return self._jresponse
    
    def prev(self, batch=None):
        '''Returns the previous batch of results.
        
        Args:
            batch: Streamed batch size. Default: None (or 100)
        '''
        self._jresponse = None
        self._jresponse = self.__query(self._query_string, batch=batch, cursor='prev')
        return self._jresponse
    
    def goto(self, cursor=0, batch=None):
        '''Returns the batch at the specified cursor position.
        
        Args:
            cursor: Cursor (or index) position. It must be integer. Default: 0.
        '''
        self._jresponse = None
        self._jresponse = self.__query(self._query_string, batch=batch, cursor=str(cursor))
        return self._jresponse
    
    def response(self):
        '''Returns the response in the form of JSON object.
        '''
        return self._jresponse
    
class PadoRpc:
    '''
    PadoRpc is a convenience class that wraps the IBiz class, com.netcrest.pado.biz.IRpcBiz.
    
    IRpcBiz provides methods to invoke DNA objects in the grid.
    '''
    _pado = None
    _ibiz = 'com.netcrest.pado.biz.IRpcBiz'
    
    def __init__(self, pado):
        self._pado = pado
        
    def create_request(self, timeout=None, id_=None, lang='python', agent=True):
        '''Creates an RPC request conforming to JSON RPC 2.0 with Pado extension.
        
        Args:
            timeout: Timeout in msec. Default is 10000 msec.
            id: Unique ID required by JSON RPC 2.0. If None, then one is assigned.
            lang: DNA language, Currently supports "python" and "java". Case sensitive.
            agent: True to run in the RPC agent in the grid. Using agent removes the startup overhead.
                If False, then a new out-of-process DNA is started.
                
        Returns:
            Request in the form of JSON object.
        '''
        jrequest = json.loads('{}') 
        jrequest['jsonrpc'] = '2.0'
        if id_ == None:
            id_ = uuid.uuid4().hex
        jrequest['id'] = id_
        jrequest['lang'] = lang
        jrequest['agent'] = agent
        if timeout != None:
            jrequest['timeout'] = timeout
        return jrequest
    
    def broadcast(self, jrequest):
        '''Broadcasts the specified request to all servers in the grid resulting all
        servers to invoke the RPC method specified in the request.
        
        Args:
            jrequest: Request JSON object
        '''
        return self._pado.invoke(self._ibiz, 'broadcast', jrequest)
    
    def on_server(self, jrequest):
        '''Executes the specified request on one of the servers in the grid.
        
        Args:
            jrequest: Request JSON object
        '''
        return self._pado.invoke(self._ibiz, 'executeOnServer', jrequest)
    
    def on_path(self, jrequest):
        '''Executes the specified request on path.
        
        Args:
            jrequest: Request JSON object
        '''
        return self._pado.invoke(self._ibiz, 'executeOnPath', jrequest)
    
class PadoRpcInvoker(PadoRpc):
    '''
    RpcInvoker directly invokes IRpc implementation class methods.
    Typically, a biz wrapper class that hides the RPC details is used to invoke
    IRpc objects, instead. Although this class can directly be used by
    applications, it is more appropriate for remotely testing IRpc classes.
    '''
    
    def __init__(self, pado):
        '''Constructs a PadoRpcInvoker object with the specified Pado object.
        
        Args:
            pado: Pado object
        '''
        super(PadoRpc, self).__init__()
        self._pado = pado
        
    def __create_invoker_parms(self, irpc_classname, method, jparams=None):
        '''Creates the invoker parameters.
        
        Args:
            irpc_classname: Fully qualified IRpc class name.
            method: Method name
            jparams: Method parameters in the form of JSON object.
            
        Returns:
            Parameters in the form of JSON object.
        '''
        jinvoker_params = json.loads('{}')
        jinvoker_params['method'] = method
        jinvoker_params['classname'] = irpc_classname
        if jparams != None:
            jinvoker_params['params'] = jparams
        return jinvoker_params
    
    def invoke_broadcast(self, irpc_classname, method, jparams=None, id=None, timeout=None, lang='python', agent=True):
        '''
        Invokes the broadcast() method with the specified IRpc object information.
        
        Args:
            irpc_classname: Fully qualified IRpc class name.
            method: Method name
            jparams: Method parameters in the form of JSON object.
            id: Unique ID required by JSON RPC 2.0. If None, then one is assigned.
            lang: DNA language, Currently supports "python" and "java". Case sensitive.
            agent: True to run in the RPC agent in the grid. Using agent removes the startup overhead.
                If False, then a new out-of-process DNA is started.
        Errors:
            ValueError if the specified lang is not supported.
        '''
        jinvoker_params = self.__create_invoker_parms(irpc_classname, method, jparams)
        jrequest = self.create_request(timeout, id, lang, agent)
        if lang == 'python':
            jrequest['classname'] = 'com.netcrest.pado.rpc.client.dna.rpc_invoker_dna.RpcInvokerDna'
        elif lang == 'java':
            jrequest['classname'] = 'com.netcrest.pado.rpc.client.dna.RpcInvokerDna'
        else:
            raise ValueError("Unsupported lang: " + lang)
        
        jrequest['method'] = 'invoke'
        jrequest['params'] = jinvoker_params
        return self.broadcast(jrequest) 

