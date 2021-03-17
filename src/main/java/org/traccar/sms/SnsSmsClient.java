/*
 * Copyright 2021 Anton Tananaev (anton@traccar.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.sms;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sns.*;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;

import com.amazonaws.services.sns.model.PublishResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.Context;
import org.traccar.config.Keys;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

public class SnsSmsClient implements SmsManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(SnsSmsClient.class);

    private final AmazonSNSAsync snsClient;

    public SnsSmsClient() {
        if (Context.getConfig().hasKey(Keys.SMS_AWS_REGION)
                && Context.getConfig().hasKey(Keys.SMS_AWS_ACCESS)
                && Context.getConfig().hasKey(Keys.SMS_AWS_SECRET)) {
            snsClient = awsSNSClient();
        } else {
            throw new RuntimeException("SNS Not Configured Properly. Please provide valid config.");
        }
    }

    public AmazonSNSAsync awsSNSClient() {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(Context.getConfig().getString(Keys.SMS_AWS_ACCESS),
                Context.getConfig().getString(Keys.SMS_AWS_SECRET));
        return AmazonSNSAsyncClientBuilder.standard().withRegion(Context.getConfig().getString(Keys.SMS_AWS_REGION))
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials)).build();
    }

    @Override
    public void sendMessageSync(String destAddress, String message, boolean command) {
        Map<String, MessageAttributeValue> smsAttributes = new HashMap<>();
        smsAttributes.put("AWS.SNS.SMS.SenderID",
                new MessageAttributeValue().withStringValue("SNS").withDataType("String"));
        smsAttributes.put("AWS.SNS.SMS.SMSType",
                new MessageAttributeValue().withStringValue("Transactional").withDataType("String"));
        snsClient.publishAsync(new PublishRequest().withMessage(message)
                .withPhoneNumber(destAddress).withMessageAttributes(smsAttributes));
    }

    @Override
    public void sendMessageAsync(String destAddress, String message, boolean command) {
        sendMessageSync(destAddress, message, command);
    }
}
