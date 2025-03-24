cd ..
timeout /t 2
docker stop svlcf
docker rm svlcf
docker rmi svlcf
docker start svlcfmysql
timeout /t 2
docker build -t svlcf .
timeout /t 5
docker run --network svlcf-net --name svlcf -e TZ=Asia/Kolkata -p 9001:9001 -v C:\Data\OneDrive:/home/Data -d svlcf:latest
timeout /t 25
cd C:/Data/Code/svlcf-scheduler
call mvn clean install
timeout /t 2
cd support
start cmd.exe /c "PostDeployment.bat"