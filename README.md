![img.png](img.png)
kubectl get nodes
kubectl apply -f redis.yaml
kubectl apply -f redisinsight.yaml
kubectl get pods
![img_1.png](img_1.png)

kind create cluster --name dev --config kind-config.yaml
kubectl port-forward service/redis 6379:6379

Baza w RedisInsight	Adres
główna (dev)	redis://default@redis:6379
testowa	redis://default@redis-test:6379

![img_2.png](img_2.png)
obraz został zbudowany i wysłany do rejestru.

$env:GITHUB_PAT | docker login ghcr.io -u Szewczus --password-stdin
docker pull ghcr.io/szewczus/redis-example:f5c8bfb377ddbc536c95a42d47141b00adce9e38
docker run --rm -p 8080:8080 -e SPRING_DATA_REDIS_HOST=host.docker.internal   ghcr.io/szewczus/redis-example:f5c8bfb377ddbc536c95a42d47141b00adce9e38