package owens.oobjloader;

// This code was written by myself, Sean R. Owens, sean at guild dot net,
// and is released to the public domain. Share and enjoy. Since some
// people argue that it is impossible to release software to the public
// domain, you are also free to use this code under any version of the
// GPL, LPGL, Apache, or BSD licenses, or contact me for use of another
// license.  (I generally don't care so I'll almost certainly say yes.)
// In addition this code may also be used under the "unlicense" described
// at http://unlicense.org/ .  See the file UNLICENSE in the repo.

import java.util.ArrayList;

public class Face {
    public final ArrayList<FaceVertex> vertices = new ArrayList<>();

    public Face() {
    }

    public void add(FaceVertex vertex) {
        vertices.add(vertex);
    }

    public String toString() {
        StringBuilder result = new StringBuilder("\tvertices: " + vertices.size() + " :\n");
        for (FaceVertex f : vertices) {
            result.append(" \t\t( ").append(f.toString()).append(" )\n");
        }
        return result.toString();
    }
}   