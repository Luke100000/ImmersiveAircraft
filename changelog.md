now, port the BB to blender and export as obj
copy the object loader from paintings
use a object map with optional animations etc
    e.g. .addObject("wing").texture("carl").materialize().animation((time, matrixstack) -> do stuff)
        .addEngine()