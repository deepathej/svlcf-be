cd ..
docker stop svlcfbe
docker rm svlcfbe
docker rmi svlcfbe
docker start svlcfmysql
timeout /t 5
docker build -t svlcfbe .
timeout /t 5
docker run --network svlcf-net --name svlcfbe -e TZ=Asia/Kolkata -p 9001:9001 -v C:\Data\OneDrive:/home/Data -d svlcfbe:latest
timeout /t 15
cd C:/Data/Code/svlcf-scheduler
call mvn clean install
cd support
start cmd.exe /c "PostDeployment.bat"
