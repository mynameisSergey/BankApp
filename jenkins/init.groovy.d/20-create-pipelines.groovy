
import jenkins.model.*
import hudson.plugins.git.*
import org.jenkinsci.plugins.workflow.job.*
import org.jenkinsci.plugins.workflow.cps.*

// Общие настройки
def repoUrl = "https://github.com/mynameisSergey/BankApp.git"
def branchName = "main"

// Массив с описанием пайплайнов
def jobs = [
        [name: "00_bank-app", path: "bank-app/Jenkinsfile"],
        [name: "01_zipkin", path: "bank-app/zipkin/Jenkinsfile"],
        [name: "02_prometheus", path: "bank-app/charts/prometheus/Jenkinsfile"],
        [name: "03_grafana", path: "bank-app/charts/grafana/Jenkinsfile"],
        [name: "04_elasticsearch", path: "bank-app/charts/elasticsearch/Jenkinsfile"],
        [name: "05_logstash", path: "bank-app/charts/logstash/Jenkinsfile"],
        [name: "06_kibana", path: "bank-app/charts/kibana/Jenkinsfile"],
        [name: "07_kafka", path: "bank-app/charts/kafka/Jenkinsfile"],
        [name: "08_keycloak", path: "bank-app/charts/keycloak/Jenkinsfile"],
        [name: "09_postgresql", path: "bank-app/postgresql/Jenkinsfile"],
        [name: "10_exchange-api", path: "bank-app/charts/exchange-api/Jenkinsfile"],
        [name: "11_exchange-generator", path: "bank-app/charts/exchange-generator/Jenkinsfile"],
        [name: "12_blocker-api", path: "bank-app/charts/blocker-api/Jenkinsfile"],
        [name: "13_notifications-api", path: "bank-app/charts/notifications-api/Jenkinsfile"],
        [name: "14_accounts-api", path: "bank-app/charts/accounts-api/Jenkinsfile"],
        [name: "15_transfer-api", path: "bank-app/charts/transfer-api/Jenkinsfile"],
        [name: "16_cash-api", path: "bank-app/charts/cash-api/Jenkinsfile"],
        [name: "17_front-ui",  path: "bank-app/charts/front-ui/Jenkinsfile"]
]

def jenkins = Jenkins.instance

jobs.each { cfg ->
    def jobName = cfg.name
    def jenkinsfilePath = cfg.path

    def job = jenkins.getItem(jobName)

    if (job == null) {
        println("Создаём Pipeline job: ${jobName}")
        job = jenkins.createProject(WorkflowJob, jobName)
    } else {
        println("Обновляем существующую Pipeline job: ${jobName}")
    }

    // SCM конфигурация
    def remoteConfigs = [new UserRemoteConfig(repoUrl, null, null, null)]
    def branchSpecs = [new BranchSpec(branchName)]

    def scm = new GitSCM(remoteConfigs, branchSpecs, false, [], null, null, [])
    def definition = new CpsScmFlowDefinition(scm, jenkinsfilePath)
    definition.setLightweight(true)

    job.setDefinition(definition)
    job.save()

    println("Job '${jobName}' готова.")
}