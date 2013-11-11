/*
 * Copyright [2013] [ShopWiki]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shopwiki.roger.example;

import com.rabbitmq.client.Address;
import com.shopwiki.roger.RabbitConnector;

/**
 * @author rtewart
 */
public class ExampleConstants {

    //private static final Address ADDRESS = new Address("localhost");

    public static final Address ADDRESS = new Address("rabbitmq.ny.shopwiki.com");
    //private static final Address ADDRESS = new Address("ny187.shopwiki.com");
    //private static final Address ADDRESS = new Address("ny197.shopwiki.com");

    //private static final Address ADDRESS = new Address("rabbit-test.ny.shopwiki.com");
    //private static final Address ADDRESS = new Address("ny188.shopwiki.com");
    //private static final Address ADDRESS = new Address("ny194.shopwiki.com");

    public static final RabbitConnector CONNECTOR = new RabbitConnector(ADDRESS);

    public static final String EXCHANGE = "example-exchange";
}
