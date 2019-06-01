#!/usr/bin/env groovy

import com.mongodb.BasicDBObject
import com.mongodb.MongoClient
import com.mongodb.client.model.Filters
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate
import com.mongodb.client.FindIterable

/**
 * This script can be used to trigger an update un bitbucket users saved in the DB
 *
 * @author vladclaudiubulimac on 2019-05-30.
 */

@Grapes([
        @Grab(group='org.mongodb', module='mongo-java-driver', version='3.10.2'),
        @Grab(group='org.json', module='json', version='20180813'),
        @Grab(group='org.slf4j', module='slf4j-simple', version='1.7.25'),
        @Grab(group='org.springframework', module='spring-web', version='5.1.7.RELEASE'),
        @Grab(group='com.fasterxml.jackson.core', module='jackson-databind', version='2.9.9')
])

def logger = LoggerFactory.getLogger("update_bitbucket_users.groovy")
logger.info 'Starting the script'

bbUsersEndpoint = 'https://api.bitbucket.org/1.0/users/{email}'
bbUsername = 'user@bitbucket.org'
bbPassword = 'password'

mongo = new MongoClient('localhost', 27017)
db = mongo.getDatabase('slackbot')
slackTeam = db.getCollection('slackTeam')

RestTemplate restTemplate = new RestTemplate()

FindIterable<JSONObject> slackTeams = slackTeam.find()
Map<String, String> members = new HashMap<>()
slackTeams.each{
    it.members.each { m -> members.put(m.slackUser.email, m.bitbucketUser.username)}
}

logger.info("Start the db update")
members.entrySet().each {
    it ->
        logger.info("Requesting data to ${bbUsersEndpoint}")
        response = restTemplate.exchange(bbUsersEndpoint, HttpMethod.GET, buildHttpEntity(bbUsername, bbPassword), String.class, it.key)
        accountId = ((JSONObject)new JSONObject(response.getBody()).get("user")).get("account_id")
        logger.info("updating ${it.key} ${it.value} with accountId ${accountId} ")
        filter = Filters.eq("members.bitbucketUser.username", it.value)
        update = new BasicDBObject(['$set': ['members.$.bitbucketUser.accountId': accountId]])
        updated = slackTeam.updateMany(filter, update)
        logger.info("${updated.getMatchedCount().toString()} records matched by the filter")
        logger.info("${updated.getModifiedCount().toString()} records updated")
}

HttpEntity buildHttpEntity(String username, String password){
    HttpHeaders headers = new HttpHeaders()
    headers.setBasicAuth(username, password)
    headers.setContentType(MediaType.APPLICATION_JSON)
    return new HttpEntity(headers)
}

logger.info 'Script finished successfully'
