## Foreach Common Java libraries

The Foreach Java Common Libraries is a set of different Java libraries (JAR) files that contain
a number of useful, reusable classes (like utilities or base classes).  These are used in different projects
and are considered production ready.

Different modules include utilities for testing, object locking, distributed locking and type conversion.

More information: https://github.com/ForeachOS/foreach-common-java-libraries

Legacy information:

* Documentation and release notes: https://foreach.atlassian.net/wiki/display/FJCL/Common+Java+libraries+Home
* Issue tracker: https://foreach.atlassian.net/projects/FJCL
* Source code: https://bitbucket.org/beforeach/common-java-libraries

Developed and maintained by [Foreach.be](https://www.foreach.be).

### Local development

Note: This section is likely out of date: testcontainers are used instead.

Some tests are AWS specific, they require [Localstack][] with the S3 service to be running.
Some tests are Azure specific, they require [Azurite][] with the blob service to be runnning.
A `docker-compose` file is available in the root of this repository, running `docker-compose up` should start the required AWS and Azure services with data being stored in `local-data/storage/localstack` & `local-data/storage/azurite`.

 > On macOS you might have to do `TMPDIR=/private$TMPDIR docker-compose up` to workaround tmp dir issues.

In order to start the test application in development mode with support for one of the cloud providers an additional profile is required, `azure` for azure support and `aws` for aws support.

### Release procedure

The release procedure can be found in the comments at the bottom of the `.gitlab-ci.yml` file.

### License

Licensed under version 2.0 of the [Apache License][https://www.apache.org/licenses/LICENSE-2.0].
