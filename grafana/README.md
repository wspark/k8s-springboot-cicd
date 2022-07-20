
# grafana 구성

다양항 수집서버의에서 제공되는 데이터를 시각화하여 보여주는 모니터링 도구이며 알림설정을 통해 여러 채널에 알람을 보낼 수 있다.
본 구성에서는 Prometheus와 연계하여 K8s 클러스터/Springboot 어플리케이션을 모니터링/알림 기능 구현을 보여줌.

## grafana 설치

설치는 Helm 을 통해 진행함.

### grafana helm repo 추가
```text
# helm 으로 repo 추가 및 설치
helm repo add grafana https://grafana.github.io/helm-charts
helm repo list 
NAME                	URL                                               
grafana             	https://grafana.github.io/helm-charts
$ helm install grafana grafana/grafana -n monitoring
```
### grafana data 저장용 PVC 생성
```text
# NFS 구성된 서버에 저장
kubectl create -f grafana-pv.yaml
kubectl create -f grafana-pvc.yaml
kubectl get pvc -n monitoring
NAME                      STATUS   VOLUME                       CAPACITY   ACCESS MODES   STORAGECLASS   AGE
grafana                   Bound    grafana-pv                   10Gi       RWX                           8h
```

### grafana deployment 수정(PVC mapping)
```text
kubectl edit deployment grafana -n monitoring

124       - name: storage
125         persistentVolumeClaim:
126           claimName: grafana

```

### grafana 접속
```text
# grafana svc를 nodeport로 노출하여 k8s 클러스터 IP로 접근 (http://10.65.41.81:30185)
kubectl -n monitoring  patch svc/grafana -p '{"spec":{"type":"NodePort"}}'
kubectl get svc -n monitoring
NAME                            TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)        AGE
grafana                         NodePort    10.107.139.164   <none>        80:30185/TCP   2d3h

# 접속 패스워드 확인(secret)
kubectl get secret --namespace monitoring grafana -o jsonpath="{.data.admin-password}" | base64 --decode ; echo
PY8gCebDmxjz5WkCSyiultcliorrCHMAPMFP5Duo
```

## grafana - prometheus 연계

* 대쉬보드를 구성하기 위해서는 데이터가 필요한데 해당 데이터를 prometheus와 연계를 하기 위해서는 Datasource 등록을 해야함
prometheus의 URL은 k8s내에 동일 프로젝트에 있어 svc주소로 접근할 수 있어 prometheus-server를 입력함

* [콘솔로긴 후 왼쪽 메뉴]
"Configuration -> DataSource ->  Add data source -> Prometheus [선택] -> URL : http://prometheus-server 입력 후 Save & Test 클릭

<img src="images/grafana-datasource-prometheus.jpg" align="center" />

### k8s 클러스터 전체 모니터링 구성

* k8s 클러스터에 대한 대쉬보드를 구성하였으며 클러스터의 노드상태/CPU/메모리/디스크/네트워크 사용량에 대한 항목을 구성하였음

<img src="images/grafana-dashboard-k8s-cluster.jpg" align="center" />

### JVM(Actuator)모니터링 구성

* SpringBoot에 Actuator 모듈을 통해 Prometheus가 읽을 수 있는 metrics를 제공하여 Grafana내에서 모니터링이 가능함.

* 기본적인 Metric 외에 SpringBoot내의 커스텀 API에 대한 모니터링이 가능하며 본 예제에서는 Micrometer의 Timer 객체를 통해 
각 API들의 응답시간을 추가하였음

<img src="images/grafana-dashboard-k8s-jvm.jpg" align="center" />


## grafana 알림설정 

* Grafana에서는 AlertRule을 통해서 알람을 여러채널(Email, Slack, LINE 등)로 보낼 수 있으며
샘플에서는 LINE 메신저로 알람을 보내도록 구성하였음(Line의 token은 https://notify-bot.line.me 에서 발급)

<img src="images/grafana-contact-point-line.jpg" align="center" />

### 알림항목

* k8s 클러스터 노드가 1개 이상 NotReady 인 경우
<img src="images/grafana-alert-cluster-nodedown.jpg" align="center" />
<img src="images/grafana-alert-cluster-nodedown2.jpg" align="center" />


* SpringBoot 스레드(Runnable Status) 임계치 이상인 경우
<img src="images/grafana-alert-spring-thread.jpg" align="center" />
<img src="images/grafana-alert-spring-thread2.jpg" align="center" />


* 임계치의 경우 운영환경에서 모니터링하여 적절한 값을 정해야하는데 알림발생을 위해 타이트하게 적용


### 문제상황 알림확인(alert status and Line)

* k8s 클러스터 노드 1개 shutdown 시
<img src="images/grafana-alert-dashboard-nodedown.jpg" align="center" />

* 부하테스트시 스레드 임계치 이상인 경우
<img src="images/grafana-alert-dashboard-thread.jpg" align="center" />

* Line 알람
<img src="images/grafana-alert-line.jpg" align="center" />


## Reference 참고 링크
* [grafana](https://github.com/grafana/helm-charts) 
* [dashboard](https://grafana.com/grafana/dashboards/13694-jvm-actuator) 

