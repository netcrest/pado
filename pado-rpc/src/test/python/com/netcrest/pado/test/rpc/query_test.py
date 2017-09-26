'''
Created on Sep 25, 2017

@author: dpark
'''
import unittest
from com.netcrest.pado.rpc.rpc import main


class QueryTest(unittest.TestCase):


    def setUp(self):
        pass


    def tearDown(self):
        pass


    def testName(self):
        request = '{"mqtthost":"ubuntu1","method":"execute_pql","params":{"pql":"jde/sales?CustomerId:sjde_US_99999"},"timeout":30000,"classname":"com.jnj.ai.rpc.biz.query_demo.QueryDemo","id":"108000561414716","lang":"python","jsonrpc":"2.0"}'
        print(request)
        main(request)


if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testName']
    unittest.main()