## Install Kafka

### Install with helm
```shell
helm install kafka oci://registry-1.docker.io/bitnamicharts/kafka \
    -f kafka-helm-values.yaml \
    -n kafka
```

### How to use
To create a pod that you can use as a Kafka client run the following commands:

```shell
kubectl run kafka-client --restart='Never' --image docker.io/bitnami/kafka:3.4.1-debian-11-r0 --namespace kafka --command -- sleep infinity
kubectl exec --tty -i kafka-client --namespace kafka -- bash

PRODUCER:
    kafka-console-producer.sh \
        --broker-list kafka-0.kafka-headless.kafka.svc.cluster.local:9092 \
        --topic test

CONSUMER:
    kafka-console-consumer.sh \
        --bootstrap-server kafka.kafka.svc.cluster.local:9092 \
        --topic test \
        --from-beginning
```
