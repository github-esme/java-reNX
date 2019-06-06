# java-reNX
NXForamt Reader Written in Java

### About NXFormat:
See https://nxformat.github.io/


### TODO

* The data inside Audio, Bitmap Nodes hasn't be parsed
* NXReadSelection not working

### Usage

#### AutoClosable
```java
try(NXFile nxfile = new NXFile("Etc.nx", NXReadSelection.EAGER_PARSE_ALL)) {

    NXNoneNode androidNode = (NXNoneNode) nxfile.resolvePath("Android");

    androidNode.forEach(childNode -> {
        
        NXString strNode = childNode.getChild("stringNode");
        String name = strNode.getName();
        String value = strNode.getValue();
        
        NXInt64Node int64Node = childNode.getChild("int64Node");
        long value2 = strNode.getValue();
        int intValue2 = strNode.toInteger();
        short shortValue2 = strNode.toShort();
        boolean boolValue2 = strNode.toBoolean();
    });

    for(NXNode<?> childNode : androidNode) {
        ...
    }

} catch (Exception e) {
    ...
}

```
