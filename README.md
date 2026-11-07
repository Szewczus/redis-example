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