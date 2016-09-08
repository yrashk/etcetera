[![Build Status](https://travis-ci.org/yrashk/etcetera.svg?branch=master)](https://travis-ci.org/yrashk/etcetera)
[![Maven Central](https://img.shields.io/maven-central/v/com.yrashk/etcetera.svg?maxAge=2592000)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%20%22com.yrashk%22%20a%3A%22etcetera%22)

# Etcetera

Typically, OSGi configuration files are limited to the filesystem and don't support directory overlaying. Etcetera addresses this by allowing to stack
arbitrary storage backends to read and write configuration.

Currently supported backends:

* file
* s3

## Installation

Etcetera is distributed as a Karaf feature. You can install it in the shell:

```
feature:repo-add mvn:com.yrashk/etcetera/LATEST/xml/features
feature:install etcetera
```

## Usage

By default, once `etcetera` feature is installed and started, there is no need to configure anything as, by default, Etcetera will provision Karaf's `etc` as file backend.

To configure backends, one must override `backends` property in `etc/etcetera.properties`:

```properties
backends = etc, cloud
```

Backends will be always processed in the order they were listed — i.e. first `etc` will be read, then `cloud`.

To define a file-based backend (`etc`), we should add:

```properties
etc.service = file
etc.path = ${karaf.etc}
```

To define an S3-based backend (`cloud`), we should add:

```properties
cloud.service = s3
cloud.bucket = myservice-etc
```

Optionally, if you are using a custom S3-compatible server (such as [Minio](http://minio.io)), you can specify your endpoint:

```properties
cloud.endpoint=http://127.0.0.1:9000
```

By default, none of the backends will store any configuration updates. To change that, a save policy can be defined for any backend:

```properties
etc.save = ALL
cloud.save = PROPERTIES
```

Available save policies:

* `NONE`: none of the updated configuration will be saved (default)
* `PROPERTIES`: only properties previously defined in a file will be saved
* `FILES`: only files that previously existed, will be updated
* `ALL`: all configuration updates will be saved

## Contributing

Contributions of all kinds (code, documentation, testing, artwork, etc.) are highly encouraged. Please open a GitHub issue if you want to suggest an idea or ask a question.

We use Unprotocols [C4 process](http://rfc.unprotocols.org/spec:1/C4). In a nutshell, this means:

* We merge pull requests rapidly (try!)
* We are open to diverse ideas
* We prefer code now over consensus later
