#!/usr/bin/env bash

# manager1 löschen
docker-machine rm manager1

# neuen manager erstellen
docker-machine create --driver virtualbox manager1

# images bauen
cd ../MainServer
sbt docker:publishLocal
cd ../WorkerServer
sbt docker:publishLocal
cd ../ActorSystem
sbt docker:publishLocal

# images speichern und auf manager schieben
docker save -o main.tar akka-http-microservice-mainserver:1.0
docker-machine scp main.tar manager1:.
docker save -o worker.tar akka-http-microservice-workerserver:1.0
docker-machine scp worker.tar manager1:.
docker save -o actor.tar actorsystem:1.0
docker-machine scp actor.tar manager1:.

# in Manager einloggen
docker-machine ssh manager1

# Images laden
docker load < main.tar
docker load < worker.tar
docker load < actor.tar

# Swarm erstellen
docker swarm init --advertise-addr 192.168.99.101
# Registry TODO

docker service create -p 27020:27020 --name mainservice akka-http-microservice-mainserver:1.0

docker save -o worker.tar akka-http-microservice-workerserver:1.0

docker-machine ssh manager1