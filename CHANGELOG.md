## [2.0.2](https://github.com/mredjem/kafka-connect-secret-registry/compare/2.0.1...2.0.2) (2026-04-19)


### Bug Fixes

* solve race condition issue on secret creation ([3587981](https://github.com/mredjem/kafka-connect-secret-registry/commit/35879817b7a10db3d45e780216fb6566bc726a35))

## [2.0.1](https://github.com/mredjem/kafka-connect-secret-registry/compare/2.0.0...2.0.1) (2026-04-18)


### Performance Improvements

* provide some minor code improvements ([55fe9b1](https://github.com/mredjem/kafka-connect-secret-registry/commit/55fe9b1fb99dacc5100e461c0520d9e6a1baf395))

# [2.0.0](https://github.com/mredjem/kafka-connect-secret-registry/compare/1.3.3...2.0.0) (2026-04-18)


### Features

* use stronger encryption algorithm ([c706300](https://github.com/mredjem/kafka-connect-secret-registry/commit/c70630010162f649af78a6f39f05bd2de6cb0a40))


### BREAKING CHANGES

* the new algorithm will not be able to decrypt cypher texts encrypted with the previous implementation

## [1.3.3](https://github.com/mredjem/kafka-connect-secret-registry/compare/1.3.2...1.3.3) (2026-04-18)


### Bug Fixes

* solve race condition when creating multiple secrets for same key ([eb27356](https://github.com/mredjem/kafka-connect-secret-registry/commit/eb273563051d8a7e4b3f1e0bdb8fae03933d12fb))

## [1.3.2](https://github.com/mredjem/kafka-connect-secret-registry/compare/1.3.1...1.3.2) (2026-04-18)


### Bug Fixes

* improve code quality based on sonar metrics ([5502eb0](https://github.com/mredjem/kafka-connect-secret-registry/commit/5502eb0d605467acb271f3430f71effea8c67684))

## [1.3.1](https://github.com/mredjem/kafka-connect-secret-registry/compare/1.3.0...1.3.1) (2026-04-16)


### Bug Fixes

* provide reliability and maintainability fixes ([f42fad9](https://github.com/mredjem/kafka-connect-secret-registry/commit/f42fad95934650d0e1e89deb08acbef2c665e01b))

# [1.3.0](https://github.com/mredjem/kafka-connect-secret-registry/compare/1.2.7...1.3.0) (2026-04-16)


### Features

* manage rbac with kafka api keys ([fbfa53c](https://github.com/mredjem/kafka-connect-secret-registry/commit/fbfa53c2d8fc050ca4d33939bd3a9e1b44260f03))

## [1.2.7](https://github.com/mredjem/kafka-connect-secret-registry/compare/1.2.6...1.2.7) (2026-04-14)


### Bug Fixes

* reset request entity stream in filter ([f843caf](https://github.com/mredjem/kafka-connect-secret-registry/commit/f843caf19c4de8f73fac9cd5e3784c1a26d7a2ba))

## [1.2.6](https://github.com/mredjem/kafka-connect-secret-registry/compare/1.2.5...1.2.6) (2026-04-14)


### Performance Improvements

* reduce loops to extract operation and resource name from request ([3c37373](https://github.com/mredjem/kafka-connect-secret-registry/commit/3c373736f532b9181b4ba62c497be86cdc914559))

## [1.2.5](https://github.com/mredjem/kafka-connect-secret-registry/compare/1.2.4...1.2.5) (2026-04-14)


### Performance Improvements

* remove some utility classes ([27b5e3e](https://github.com/mredjem/kafka-connect-secret-registry/commit/27b5e3e69c3d43f3bb8e915ba8ef2d3041434254))

## [1.2.4](https://github.com/mredjem/kafka-connect-secret-registry/compare/1.2.3...1.2.4) (2026-04-12)


### Bug Fixes

* filter available claims variables in cel filter ([5a64af4](https://github.com/mredjem/kafka-connect-secret-registry/commit/5a64af4ad99fa9ef1d2e4c9fa5b5081b6daf5f27))

## [1.2.3](https://github.com/mredjem/kafka-connect-secret-registry/compare/1.2.2...1.2.3) (2026-04-12)


### Bug Fixes

* provide minor improvements to codebase ([335fa4b](https://github.com/mredjem/kafka-connect-secret-registry/commit/335fa4bc2d00bddaafa55a8c543046c7ddec7bcb))

## [1.2.2](https://github.com/mredjem/kafka-connect-secret-registry/compare/1.2.1...1.2.2) (2026-04-12)


### Performance Improvements

* remove mock server startup thread ([2dc6d20](https://github.com/mredjem/kafka-connect-secret-registry/commit/2dc6d20985a560ae47f6d87f6ac1a8b54d5bde61))

## [1.2.1](https://github.com/mredjem/kafka-connect-secret-registry/compare/1.2.0...1.2.1) (2026-04-11)


### Bug Fixes

* solve minor coding warnings ([50b672f](https://github.com/mredjem/kafka-connect-secret-registry/commit/50b672f1a97a025e614bdddc69ed37c73c80ffbd))

# [1.2.0](https://github.com/mredjem/kafka-connect-secret-registry/compare/1.1.0...1.2.0) (2026-04-11)


### Features

* improve and fix issues with rbac management ([5811c46](https://github.com/mredjem/kafka-connect-secret-registry/commit/5811c46530573a2b49303b6274aad2e0f93f8d3a))

# [1.1.0](https://github.com/mredjem/kafka-connect-secret-registry/compare/1.0.0...1.1.0) (2026-04-09)


### Features

* manage rbac stored in confluent cloud ([59ea293](https://github.com/mredjem/kafka-connect-secret-registry/commit/59ea29310fc3f4d6602e5e403e0981f329d15e33))

# [1.0.0](https://github.com/mredjem/kafka-connect-secret-registry/compare/0.1.4...1.0.0) (2026-04-05)


### Features

* provide rbac management for bearer authentication ([adbd4ca](https://github.com/mredjem/kafka-connect-secret-registry/commit/adbd4ca0a0325ac4c754491cff8c2e34e948b16b))


### BREAKING CHANGES

* jwt now requires kafka connect roles claims to be present

## [0.1.4](https://github.com/mredjem/kafka-connect-secret-registry/compare/0.1.3...0.1.4) (2026-03-29)


### Bug Fixes

* solve logback cve ([ab918da](https://github.com/mredjem/kafka-connect-secret-registry/commit/ab918daddea127983fae7a80b461eb1e7329473b))

## [0.1.3](https://github.com/mredjem/kafka-connect-secret-registry/compare/0.1.2...0.1.3) (2026-03-29)


### Bug Fixes

* enforce presence of super admins configuration ([de89f9f](https://github.com/mredjem/kafka-connect-secret-registry/commit/de89f9ff512c18c781b74dac1cb5dc0f137ac9df))

## [0.1.2](https://github.com/mredjem/kafka-connect-secret-registry/compare/0.1.1...0.1.2) (2026-03-28)


### Bug Fixes

* allow access to connector status anonymously ([bd31fc8](https://github.com/mredjem/kafka-connect-secret-registry/commit/bd31fc819a03f3ed99995d721e968b2c511ebcf1))

## [0.1.1](https://github.com/mredjem/kafka-connect-secret-registry/compare/0.1.0...0.1.1) (2026-03-24)


### Bug Fixes

* allow internal requests ([f60ffec](https://github.com/mredjem/kafka-connect-secret-registry/commit/f60ffec2902e4e4c84f42e586ac78f20a03640bb))
* detect empty strings for scope ([5209328](https://github.com/mredjem/kafka-connect-secret-registry/commit/5209328de0224e6754898043f8e518f018bde825))
* shorten kv store readiness wait ([51ae8b9](https://github.com/mredjem/kafka-connect-secret-registry/commit/51ae8b9f9e7cf4476cfdc068fb9fcfb413548068))

# [0.1.0](https://github.com/mredjem/kafka-connect-secret-registry/compare/0.0.10...0.1.0) (2026-03-23)


### Features

* wait for kv store to be ready ([d3ead90](https://github.com/mredjem/kafka-connect-secret-registry/commit/d3ead9092cb4afc617b72901c4f8209630c76ff1))

## [0.0.10](https://github.com/mredjem/kafka-connect-secret-registry/compare/0.0.9...0.0.10) (2026-03-22)


### Bug Fixes

* resolve dependencies conflicts ([9a3cd99](https://github.com/mredjem/kafka-connect-secret-registry/commit/9a3cd998cfd54d2a2683668b8bb03c5688080df2))

## [0.0.9](https://github.com/mredjem/kafka-connect-secret-registry/compare/0.0.8...0.0.9) (2026-03-21)


### Bug Fixes

* solve typo in manifest ([c63a7e0](https://github.com/mredjem/kafka-connect-secret-registry/commit/c63a7e0e4746ce34c190c8e0104b2cbf721602f5))

## [0.0.8](https://github.com/mredjem/kafka-connect-secret-registry/compare/0.0.7...0.0.8) (2026-03-21)


### Bug Fixes

* solve bearer filter error when running admin client ([821e50b](https://github.com/mredjem/kafka-connect-secret-registry/commit/821e50b9189f4403ffaba4b9c9f75e8c51b6ae82))

## [0.0.7](https://github.com/mredjem/kafka-connect-secret-registry/compare/0.0.6...0.0.7) (2026-03-20)


### Bug Fixes

* properly ignore empty super admin scope ([7448116](https://github.com/mredjem/kafka-connect-secret-registry/commit/744811639c66c5b42c97fa1cc2d67f69b6cf034e))

## [0.0.6](https://github.com/mredjem/kafka-connect-secret-registry/compare/0.0.5...0.0.6) (2026-03-20)


### Bug Fixes

* properly stop consumer thread pool ([52cbe41](https://github.com/mredjem/kafka-connect-secret-registry/commit/52cbe41f530e9586676c4ab76384d7f58d65e74a))

## [0.0.5](https://github.com/mredjem/kafka-connect-secret-registry/compare/0.0.4...0.0.5) (2026-03-20)


### Bug Fixes

* use jaxrs exceptions instead of custom ones ([3c546eb](https://github.com/mredjem/kafka-connect-secret-registry/commit/3c546eb330a716e4678c8685146047b096af012d))

## [0.0.4](https://github.com/mredjem/kafka-connect-secret-registry/compare/0.0.3...0.0.4) (2026-03-19)


### Bug Fixes

* prevent manifest duplication because of sed command ([bbd01d4](https://github.com/mredjem/kafka-connect-secret-registry/commit/bbd01d419eef707a72c2065a9a99ff9d472ca4bc))

## [0.0.3](https://github.com/mredjem/kafka-connect-secret-registry/compare/0.0.2...0.0.3) (2026-03-18)


### Bug Fixes

* add manifest to the list of files to commit ([991e885](https://github.com/mredjem/kafka-connect-secret-registry/commit/991e885c5901ee85da41b5cb48000f6b99732e7b))
* rename artifacts to be compliant with confluent specification ([cc86499](https://github.com/mredjem/kafka-connect-secret-registry/commit/cc86499127a59b4edc2ef734640bf3b5aea2c057))
