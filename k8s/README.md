# kubernetes 구성

## 구성 현황 ###

* k8s 관리를 위한 Bastion 서버를 추가생성

| OS Verison   | IP             | Server Type    | HostName               |     Spect       |
| :----------  | :----------:    | :----------    | :--------------------: | :-------------: |
| CentOS 7.9    | 10.65.40.80    | Management     | wspark-kube-bastion    | 2vcpus, 8G Ram |
| CentOS 7.9    | 10.65.40.81    | Master         | wspark-kube-mas01      | 2vcpus, 4G Ram |
| CentOS 7.9    | 10.65.40.84    | Worker         | wspark-kube-worker01   | 2vcpus, 8G Ram |
| CentOS 7.9    | 10.65.40.85    | Worker         | wspark-kube-worker02   | 2vcpus, 8G Ram |

## All Nodes 
### OS update and reboot
```text
yum -y update && sudo systemctl reboot
```

###  kubernetes repo 준비
```text
sudo tee /etc/yum.repos.d/kubernetes.repo<<EOF
[kubernetes]
name=Kubernetes
baseurl=https://packages.cloud.google.com/yum/repos/kubernetes-el7-x86_64
enabled=1
gpgcheck=1
repo_gpgcheck=1
gpgkey=https://packages.cloud.google.com/yum/doc/yum-key.gpg https://packages.cloud.google.com/yum/doc/rpm-package-key.gpg
EOF
```
### kubernetes 설치
```text
yum clean all && sudo yum -y makecache
yum -y install epel-release vim git curl wget kubelet kubeadm kubectl --disableexcludes=kubernetes
```
### firewalld 비활성화
* 테스으 환경으로 firewalld 비활성화
```text
systemctl disable --now firewalld
```
### selinux & swap 비활성
```text
setenforce 0
sed -i 's/^SELINUX=.*/SELINUX=permissive/g' /etc/selinux/config

sed -i '/ swap / s/^\(.*\)$/#\1/g' /etc/fstab
swapoff -a
```

### sysctl 수정
```text
modprobe overlay
modprobe br_netfilter

sudo tee /etc/sysctl.d/kubernetes.conf<<EOF
net.bridge.bridge-nf-call-ip6tables = 1
net.bridge.bridge-nf-call-iptables = 1
net.ipv4.ip_forward = 1
net.ipv6.conf.all.disable_ipv6=0
net.ipv6.conf.default.disable_ipv6=0
net.ipv6.conf.tun0.disable_ipv6=0
EOF

sudo sysctl --system
```
### 컨테이너 Runtime 환경 설치
* 컨테이너 실행환경은 Docker, CRI-O, Containered 로 구성가능한데 Docker는 1.24 이후 제외예정이라 CRI-O 설치로 구성
```text
OS=CentOS_7
VERSION=1.24
curl -L -o /etc/yum.repos.d/devel:kubic:libcontainers:stable.repo https://download.opensuse.org/repositories/devel:/kubic:/libcontainers:/stable/$OS/devel:kubic:libcontainers:stable.repo
curl -L -o /etc/yum.repos.d/devel:kubic:libcontainers:stable:cri-o:$VERSION.repo https://download.opensuse.org/repositories/devel:kubic:libcontainers:stable:cri-o:$VERSION/$OS/devel:kubic:libcontainers:stable:cri-o:$VERSION.repo

# Update CRI-O Subnet
sed -i 's/10.85.0.0/192.168.0.0/g' /etc/cni/net.d/100-crio-bridge.conf

# Start and enable Service
systemctl daemon-reload
systemctl start crio
systemctl enable crio
```
### /etc/host 등록
```text
10.65.41.81 wspark-kube-mas01
10.65.41.84 wspark-kube-worker01
10.65.41.85 wspark-kube-worker02
```
## Master Nodes 

### kubectl enable
```text
systemctl enable kubelet
systemctl start kubelet
```
### kubernetes 컨테이너 이미지 Pull
```
kubeadm config images pull
[config/images] Pulled k8s.gcr.io/kube-apiserver:v1.24.2
[config/images] Pulled k8s.gcr.io/kube-controller-manager:v1.24.2
[config/images] Pulled k8s.gcr.io/kube-scheduler:v1.24.2
[config/images] Pulled k8s.gcr.io/kube-proxy:v1.24.2
[config/images] Pulled k8s.gcr.io/pause:3.7
[config/images] Pulled k8s.gcr.io/etcd:3.5.3-0
[config/images] Pulled k8s.gcr.io/coredns/coredns:v1.8.6

```

### cluster 생성
```text
kubeadm init --pod-network-cidr=192.168.0.0/16 --upload-certs --control-plane-endpoint=wspark-kube-mas01
```

### kubeconfig 복사
```text
mkdir -p $HOME/.kube
cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
chown $(id -u):$(id -g) $HOME/.kube/config
```
### master node join
```text
 kubeadm join wspark-kube-mas01:6443 --token uoha55.g7mswz8z2n94sosk .--discovery-token-ca-cert-hash sha256:b186bd08e160d4cabf959091b7c1f6be3ff623986f9e7bea852938cd10fb00f0
  --control-plane 
```

## Worker Node 생성

### worekr노드 join
```text
# wspark-kube-worker01
 kubeadm join wspark-kube-mas01:6443 --token uoha55.g7mswz8z2n94sosk .--discovery-token-ca-cert-hash sha256:b186bd08e160d4cabf959091b7c1f6be3ff623986f9e7bea852938cd10fb00f0
# wspark-kube-worker02
 kubeadm join wspark-kube-mas01:6443 --token uoha55.g7mswz8z2n94sosk .--discovery-token-ca-cert-hash sha256:b186bd08e160d4cabf959091b7c1f6be3ff623986f9e7bea852938cd10fb00f0
```

## netowrk plugin 설치
```
kubectl create -f https://docs.projectcalico.org/manifests/tigera-operator.yaml 
kubectl create -f https://docs.projectcalico.org/manifests/custom-resources.yaml
```

### cluster node 확인
```
kubectl get nodes
NAME                   STATUS   ROLES    AGE    VERSION
wspark-kube-mas01      Ready    master   4d3h   v1.24.2
wspark-kube-worker01   Ready    worker   4d3h   v1.24.2
wspark-kube-worker02   Ready    worker   4d3h   v1.24.2
```

## Bastion 서버에서 작업
* 클러스터 구성 후 Bastion 서버에서 클러스터 작업을 위한 패키지 설치
```
yum install kubelet -y

# k8s config 파일 복사
cp wspark-kube-mas01:/root/.kube/config /root/.kube/config

# Node 확인
kubectl get nodes
NAME                   STATUS    AGE
wspark-kube-mas01      Ready     4d
wspark-kube-worker01   Ready     4d
wspark-kube-worker02   Ready     4d
[root@bastion git]# 

```

## Reference 참고 링크
* [kubernetes install](https://kubernetes.io/docs/setup/production-environment/tools/kubeadm/install-kubeadm/)
* [install blog](https://computingforgeeks.com/install-kubernetes-cluster-on-centos-with-kubeadm/)