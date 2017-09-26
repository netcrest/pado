## Pado RPC

The Pado RPC mechanism is an add-on component that enables running apps written in any languages locally in each data node. A data node app (DNA) is an app that is installed on a data node to primarily work with that data node's dataset. To achieve this, pado-rpc establishes interprocess communications between DNA and the data node, conforming to JSON RPC 2.0 with its own extensions.

## Why DNA (Data Node App)

DNA offers several benefits with some tradeoffs.

### Benefits
- Non-intrusive. It isolates computations in separate processes per data node. Each DNA is launched in a separate process running independently. They are loosely coupled with the data node via a set of API that conforms to JSON RPC 2.0. The RPC mechanism is completely hidden under the API and the API is comprised of functions and classes that are specific to a target language.
- Auto data aggregation - pado-rpc automatically aggregates the results returned by individual DNAs for the client application. A client application such as a browser invokes a Pado's IBiz business object method via REST, which in turn invokes DNAs and aggregates theirs results back to the client in the form of JSON object.
- Real-time data change listener. Any grid path data changes can be listened on in real-time.
- Data dump. Any grid path can be dumped to a local file accessible by DNAs.
- Language agnostic. DNA can be written in various languages. Currently, Java and Python are supported. R, C/C++, and Scalar are planned in the near future.

### Tradeoffs
- pado-rpc relies on IPC for communications between DNA and the data node. Although the IPC mechanics leverage loopback connections, there are some performance impacts. It will be significantly slower than in-process operations done in the data node. For this reason, pado-rpc should not be used for services that require low latency return operations. As a general guideline, if the required latency is 10 msec or less then pado-rpc may not be a good choice.
- Because it runs as a separate process, the API must be built from scratch. The initial API may not be as rich as you wish but the goal is to provide a complete set of API in the near future.


## Use Cases

Support for DNA came into shape primarily due to machine learning requirements. In a data grid, there is not much you can do in terms of parallelizing the existing ML algorithms unless you heavily customize them. This is because they typically require the entire data set in a single process to create models. Customization is always a time-consuming and error-prone task. With DNA, our goal is to reduce the customization but at the same time, allow an easy way to build new or enhanced algorithms that can leverage the power of parallelization. There are a number of ML use cases that can benefit from pado-rpc. For example, in the predictive analytics area, a model can potentially be created and trained with real-time data. For neural networks, each data node can represent a neuron with hidden neurons represented by DNAs. We'll be documenting these use cases with more concrete examples in the future.

