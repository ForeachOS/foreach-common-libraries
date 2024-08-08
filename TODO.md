
The Across `file-manager-module` actually consists of three layers:

1. `FileManager` and `FileRepository` and all the implementations.

2. `FileReference`, which is a JPA entity, but it is hard to factor
   out because of all the dependencies on the Hibernate module and the
   Properties module. This isn't used by project C, but is used by
   project MI. `FileReferenceProperties` does not appear to be used
   anywhere.

3. The admin UI, based on `entity-module` and
   `admin-web-module`. We'll replace this, and for browsing the
   blob/file repositories, we can use the native tools or the IntelliJ
   Big Data Tools plugin.

So for now, I factored out only layer 1, into the
`common-file-manager` module.

Layer 2 could become `common-file-manager-jpa`, and layer 3 can simply
be deleted.
