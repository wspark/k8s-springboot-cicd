
# k8s 환경에서 컨테이너 배포

## 기본 프로젝트 생성 및 deployment 생성

```text
# namespace 생성
kubectl create namespace wspark

# deployment 생성
kubectl create deployment springboot-demo --image docker.io/wspark83/springboot:demo-v1.0  -n wspark

# 외부접속용 svc nodeport 변경
kubectl expose deployment springboot-demo --port 8080 --target-port 8080 --type NodePort -n wspark

# nodeport 확인
kubectl get svc -n wspark
NAME              TYPE       CLUSTER-IP      EXTERNAL-IP   PORT(S)          AGE
springboot-demo   NodePort   10.101.65.210   <none>        8080:31236/TCP   2d1h

# 호출
curl 10.65.41.81:31236/api/library/author
[{"id":1,"firstName":"Wspark","lastName":"Ko"},{"id":2,"firstName":"KimTaeHee","lastName":"Ko"},{"id":3,"firstName":"parkwonseok","lastName":"Ko"}]


# PV/PVC 생성
kubectl create -f springboot-demo-pv.yaml
kubectl create -f springboot-demo-pvc.yaml

# deployment에 volume 추가(/logs 디렉토리에 springboot-demo-pvc 연결)
kubectl edit deploy springboot-sample -n wspark

    spec:
      containers:
      - image: docker.io/wspark83/springboot:demo-v1.8
        imagePullPolicy: IfNotPresent
        name: springboot
        resources: {}
        terminationMessagePath: /dev/termination-log
        terminationMessagePolicy: File
        volumeMounts:
        - mountPath: /logs
          name: springboot-demo-pvc
      dnsPolicy: ClusterFirst
      restartPolicy: Always
      schedulerName: default-scheduler
      securityContext: {}
      terminationGracePeriodSeconds: 30
      volumes:
      - name: springboot-demo-pvc
        persistentVolumeClaim:
          claimName: springboot-demo-pvc


# pod에서 pvc 확인
# pod name 확인 및 pod 내부진입
kubectl get pods -n wspark
NAME                               READY   STATUS    RESTARTS   AGE
springboot-demo-6bd844df67-99bj2   1/1     Running   0          12m
springboot-demo-6bd844df67-xhjp7   1/1     Running   0          12m
kubectl exec -ti springboot-demo-6bd844df67-99bj2 /bin/bash -n wspark

# pod 내에서 볼륨정보 추가확인(/logs)
df -h
Filesystem                                      Size  Used Avail Use% Mounted on
overlay                                          32G   12G   20G  37% /
tmpfs                                            64M     0   64M   0% /dev
tmpfs                                           3.9G     0  3.9G   0% /sys/fs/cgroup
shm                                              64M     0   64M   0% /dev/shm
tmpfs                                           3.9G  410M  3.5G  11% /etc/hostname
10.65.41.80:/data/nfs/wspark/springboot-sample  100G  2.0G   99G   2% /logs
/dev/mapper/centos-root                          32G   12G   20G  37% /etc/hosts
tmpfs                                           7.7G   12K  7.7G   1% /run/secrets/kubernetes.io/serviceaccount
tmpfs                                           3.9G     0  3.9G   0% /proc/acpi
tmpfs                                           3.9G     0  3.9G   0% /proc/scsi
tmpfs                                           3.9G     0  3.9G   0% /sys/firmware

```

## autoscale 설정
```
# hpa 설정으로 cpu, mem 임계치를 초과할 경우 자동확장 기능사용
kubectl create -f hpa.yaml -n wspark
horizontalpodautoscaler.autoscaling/hpa created
kubectl get hpa -n wspark
NAME   REFERENCE                    TARGETS   MINPODS   MAXPODS   REPLICAS   AGE
hpa    Deployment/springboot-demo   1%/60%    1         3         3          21s

# project limits 추가
kubectl create -f limits.yaml -n wspark
limitrange/limit-range created
kubectl get limits -n wspark
NAME          CREATED AT
limit-range   2022-07-22T02:33:57Z

# project quota 추가
kubectl create -f quota.yaml -n wspark
resourcequota/quota created
kubectl get quota -n wspark
NAME    AGE   REQUEST                                            LIMIT
quota   9s    requests.cpu: 600m/4, requests.memory: 768Mi/4Gi   limits.cpu: 6/8, limits.memory: 1536Mi/8Gi

# deployment 에 resources 제한
kubectl edit deploy -n wspark
     containers:
      - image: docker.io/wspark83/springboot:demo-v1.8
        imagePullPolicy: IfNotPresent
        name: springboot-demo
        resources:
          limits:
            cpu: "2"
          requests:
            cpu: 200m

# 서비스에 부하주기
ab -n 1000 -c 1000 http://10.65.41.81:31236/api/library/book

# 부하시 hpa개수 max까지 증가
 kubectl get hpa -n wspark
NAME   REFERENCE                    TARGETS    MINPODS   MAXPODS   REPLICAS   AGE
hpa    Deployment/springboot-demo   424%/60%   1         3         3          7m4s
[root@wspark-kube-mas01 ~]# 

```

## Reference 참고 링크
* [kubernetes] (https://kubernetes.io/)
* [kubernetes autoscale] (https://kubernetes.io/docs/tasks/run-application/horizontal-pod-autoscale-walkthrough/#autoscaling-on-more-specific-metrics)