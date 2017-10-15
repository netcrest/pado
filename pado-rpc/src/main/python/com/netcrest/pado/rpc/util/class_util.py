'''
Created on Oct 15, 2017

@author: dpark
'''
import inspect

def get_class_name(obj):
    '''Returns class name of the specified object.
    
    Args:
        obj: Any object.
    '''
    c = obj.__class__.__mro__[0]
    return c.__module__ + "." + c.__name__

def get_class_introspect(method):
    '''Returns the class of the specified method.
    
    Args:
        method: Class method. 
    
    Returns: None if invalid method.
    '''
    if inspect.ismethod(method):
        for cls in inspect.getmro(method.__self__.__class__):
            if cls.__dict__.get(method.__name__) is method:
                return cls
        method = method.__func__  # fallback to __qualname__ parsing
    if inspect.isfunction(method):
        cls = getattr(inspect.getmodule(method),
                      method.__qualname__.split('.<locals>', 1)[0].rsplit('.', 1)[0])
        if isinstance(cls, type):
            return cls
    return None

def get_class_name_introspect(method):
    '''
    Returns the fully-qualified name (including module name) of the specified method.
    
    Args:
        method: Class method. 
    
    Returns: None if invalid method.
    '''
    c = get_class_introspect(method)
    if c == None:
        return None
    else:
        return c.__module__ + "." + c.__name__
    
def get_class_method_names(class_method_name):
    '''
    Returns the class and method name extracted from the specified fully-qualfied method name.
    
    Args:
        class_method_name: Fully-qualified method name that includes the module name
            the method name in dot notations.
            
    Returns:
        class_name, method_name.
    '''
    index = class_method_name.rindex('.')
    classname = class_method_name[0:index]
    method_name = class_method_name[index+1:]
    return classname, method_name    

