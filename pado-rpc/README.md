## Pado RPC

The Pado RPC mechanism is an add-on component that enables Pado to locally run apps written in any languages in each data node. A data node app (DNA) is an app that is installed on a data node to primarily work with that data node's dataset. To achieve this, pado-rpc establishes interprocess communications (IPC) between DNA and the data node, conforming to JSON RPC 2.0 with its own extensions.

## DNA Execution Modes

There are two distinct execution modes in Pado RPC: _non-agent_ and _agent_. In the non-agent mode, each DNA is executed in their own process. This completely isolates non-agent DNAs from each other thereby removing any dependencies. This maybe ideal for DNAs that can work alone and tolerate the relatively high initial startup latency. For those DNAs that are sensitive to the startup latency, you can execute them in the agent mode, which handles DNA executions in a dedicated agent process that already has RPC connections established. In the agent mode, each DNA is executed in a dedicated thread in the same process space as the agent. This removes the startup latency and also allows individual DNAs access each other by reference. By default, a DNA is run in the agent mode.

## Why DNA?

The primary purpose of DNAs is to provide support for out-of-process executions on local datasets of individual data nodes. Typically, we embed business rules and logic in the form of business objects (IBiz) in the same process space as the data node that stores local datasets. This works well for the language that the data node is written in, i.e., Java in our case, and provides the best performance. However, this also means that its intrusiveness could potentially harm the data node if there are misbehaving business objects. 

With DNA, since there are running out-of-process components, they do not directly affect data nodes. They are equivalent to client apps running in the same environment as data nodes. Because of this level of isolation, DNA can freely run with no in-process performance and security impacts to data nodes. A DNA can run in the same machine as a data node or in a separate machine. This flexibility allows DNAs to tackle computational problems in any languages without affecting individual data nodes. DNAs are part of the data grid yet they are completely isolated.

DNA offers several benefits with some tradeoffs as described below.

### Benefits
- _Non-intrusive_. It isolates computations in separate processes per data node. Each DNA is launched in a separate process running independently. They are loosely coupled with the data node via a set of API that conforms to JSON RPC 2.0. The RPC mechanism is completely hidden under the API and the API is comprised of functions and classes that are specific to a target language.
- _Auto data aggregation_. Pado RPC automatically aggregates the results returned by individual DNAs for the client application. A client application such as a browser invokes a Pado's IBiz business object method via REST, which in turn invokes DNAs and aggregates theirs results back to the client in the form of JSON object.
- _Real-time data change listener_. Any grid path data changes can be listened on in real-time.
- _Data dump_. Any grid path can be dumped to a local file accessible by DNAs.
- Language agnostic. DNA can be written in various languages. Currently, Java and Python are supported. R, C/C++, and Scalar are planned in the near future.

### Tradeoffs
- Pado RPC relies on IPC for communications between DNA and the data node. There is a slight IPC overhead that may be significant for low latency applications. If your application has end-to-end sub-millisecond latency requirements then you should use Pado's IBiz instead.
- Because it runs as a separate process, the DNA API must be built from scratch. The Python DNA API is not as rich as Java. 

## Use Cases

Support for DNA came into shape primarily due to machine learning requirements. In a data grid, there is not much you can do in terms of parallelizing the existing ML algorithms unless you heavily customize them. This is because they typically require the entire dataset in a single process to create models. Customization is always a time-consuming and error-prone task. With DNA, our goal is to reduce the customization efforts and allow an easy way to build new or enhanced algorithms that can leverage the power parallelization offered by data grids. There are a number of ML use cases that can benefit from Pado RPC. For example, in the predictive analytics area, a model can potentially be dynamically created and trained with real-time data. For neural networks, each data node may represent an outer neuron with hidden neurons represented by DNAs. We'll be documenting these use cases with more concrete examples in the future.

## Invoking DNAs
DNAs can be invoked via [IRpcBiz](../pado-rpc-biz/src/main/java/com/netcrest/pado/biz/IRpcBiz.java). For Python, the module [com.netcrest.pado.rpc.client.rest.pado.py](../pado-rpc/src/main/python/com/netcrest/pado/rpc/client/rest/pado.py) provides the `Pado` class for executing the RESTful API provided byt [**pado-web**](https://github.com/netcrest/pado-web). 

