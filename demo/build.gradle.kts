import java.util.Properties
import java.io.FileInputStream

subprojects{
    repositories {
        mavenLocal()
        mavenCentral()
        mavenCentral {
            metadataSources {
                artifact()
            }
        }
    }
}