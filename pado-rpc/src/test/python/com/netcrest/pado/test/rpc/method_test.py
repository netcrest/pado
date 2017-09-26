'''
Created on Sep 26, 2017

@author: dpark
'''
import json
import unittest

from com.netcrest.pado.rpc.util.rpc_util import invoke


class MethodTest(unittest.TestCase):


    def testInvoke(self):
        request = '{"mqtthost":"ubuntu1","method":"execute_pql","params":{"pql":"jde/sales?CustomerId:sjde_US_99999"},"timeout":30000,"classname":"com.jnj.ai.rpc.biz.query_demo.QueryDemo","id":"108000561414716","lang":"python","jsonrpc":"2.0"}'
        print(request)
        jrequest = json.loads(request)
        invoke("rpc_test", jrequest)


if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testInvoke']
    unittest.main()