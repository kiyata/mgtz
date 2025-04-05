# Running Gatling in Robot Framework

## setup gatling project in sbt
Setup gatling project in maven, sbt or gradle. Here we are using sbt setup

1. build.sbt

```
enablePlugins(GatlingPlugin)

scalaVersion := "2.13.5"

val gatlingVersion = "3.7.4"
libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion % "test"
libraryDependencies += "io.gatling"            % "gatling-test-framework"    % gatlingVersion % "test"
```

2. project/plugins.sbt
```addSbtPlugin("io.gatling" % "gatling-sbt" % "4.1.2")```

3. project/build.properties
```sbt.version=1.6.2```

4. Follow Gatling project structure to create your tests. Here we are creating tests in:
```src/test/simulations```

## to run test
sbt Gatling/test -DENV=dev


///teks/admin/srp/reject_reason/program/10172631/expectation/10173631/correlation/10173636/citation/10184151?_wrapper_format=drupal_modal&ajax_form=1&_wrapper_format=drupal_ajax
            