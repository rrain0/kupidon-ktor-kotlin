$imageVersion = read-host "Enter docker image version"
write-host "Image version: $imageVersion"
read-host "Press any key to build & push docker image..."
docker build -t rrain0/kupidon-ktor-ktor:$imageVersion .
docker push rrain0/kupidon-ktor-ktor:$imageVersion
read-host "All operations FINISHED!"