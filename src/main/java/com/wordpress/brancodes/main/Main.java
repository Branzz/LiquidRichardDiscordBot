package com.wordpress.brancodes.main;

import com.wordpress.brancodes.bot.LiquidRichardBot;
import com.wordpress.brancodes.messaging.reactions.Reactions;

import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {

	private static LiquidRichardBot bot;

	public static void main(String... args) {

		// PoolConnection.begin();

		try {
			bot = new LiquidRichardBot();
		} catch (LoginException e) {
			e.printStackTrace();
		}

//		if (args.length > 1)
//			upsertCommands(new HashSet<>(List.of(args)));

		Runtime.getRuntime().addShutdownHook(new Thread(Reactions::flushAutoDeleteQueue));

		try (InputStreamReader iReader = new InputStreamReader(System.in);
			 BufferedReader bReader = new BufferedReader(iReader)) {

//			while (true) {
				String[] input = bReader.readLine().split("\\s+");
//				if (input[0].equalsIgnoreCase("upsert")) {
//					upsertCommands(new HashSet<>(Arrays.asList(input).subList(1, input.length)));
//				} else {
//					break;
//				}
//			}

			// PoolConnection.end();

			System.exit(0);

		} catch (IOException e) {
			e.printStackTrace();
		}


	}
/*

C:/Users/Brock/.jdks/openjdk-16.0.1/bin/java.exe
C:/Users/Brock/.jdks/openjdk-16.0.1/bin/java.exe -Dfile.encoding=windows-1252 -Duser.country=US -Duser.language=en -Duser.variant -cp C:\Users\Brock\intellij-workspaces\LiquidRichardBot0.0.2\build\classes\java\main;C:\Users\Brock\intellij-workspaces\LiquidRichardBot0.0.2\build\resources\main;C:\Users\Brock\intellij-workspaces\LiquidRichardBot0.0.2\libs\main.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\net.dv8tion\JDA\5.0.0-alpha.2\cb45d0da0d6f5977b3ad86978d4fd0736be8fdf9\JDA-5.0.0-alpha.2.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\org.springframework.boot\spring-boot-starter-data-jpa\2.6.4\f12f4ceb6765c1ab892a6bc72dad5aaad670a87f\spring-boot-starter-data-jpa-2.6.4.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\org.springframework.boot\spring-boot-starter-aop\2.6.4\13e3695074a176ca4e22543b084943e5932b3b5f\spring-boot-starter-aop-2.6.4.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\org.springframework.boot\spring-boot-starter-jdbc\2.6.4\19047bf41d1f01a3b5e82f90ac77dceefadda943\spring-boot-starter-jdbc-2.6.4.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\org.springframework.boot\spring-boot-starter\2.6.4\31adf5f726b6a5703815b99056110b96db7eff58\spring-boot-starter-2.6.4.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\org.springframework.boot\spring-boot-starter-logging\2.6.4\e8bab752fd29797df304ef2ad8575e5392d96c4c\spring-boot-starter-logging-2.6.4.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\ch.qos.logback\logback-classic\1.2.10\f69d97ef3335c6ab82fc21dfb77ac613f90c1221\logback-classic-1.2.10.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\org.apache.commons\commons-dbcp2\2.8.0\39c8efd449f66e71589ee1c385f8f828e068f9fd\commons-dbcp2-2.8.0.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\com.github.mifmif\generex\1.0.2\b378f873b4e8d7616c3d920e2132cb1c87679600\generex-1.0.2.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\com.sedmelluq\lavaplayer\1.3.75\d29fbcd2a749fd8cbc8da7ea44d5667838078afa\lavaplayer-1.3.75.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\com.googlecode.json-simple\json-simple\1.1.1\c9ad4a0850ab676c5c64461a05ca524cdfff59f1\json-simple-1.1.1.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\org.springframework.data\spring-data-jpa\2.6.2\aa7da518dee4b20433c7e055ef5f2cda584f412f\spring-data-jpa-2.6.2.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\org.springframework.boot\spring-boot-autoconfigure\2.6.4\36e75a2781fc604ac042945eed8be2fe049731df\spring-boot-autoconfigure-2.6.4.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\org.springframework.boot\spring-boot\2.6.4\356c0ee25794ca46d8344d13cffbc30bfae1dc0e\spring-boot-2.6.4.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\org.springframework\spring-context\5.3.16\efb9c749b335bf62dc07c1674e9d76d382a027e5\spring-context-5.3.16.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\org.postgresql\postgresql\42.3.3\6f639af368afda4c5a3e77a6299262c501e67076\postgresql-42.3.3.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\net.sf.trove4j\trove4j\3.0.3\42ccaf4761f0dfdfa805c9e340d99a755907e2dd\trove4j-3.0.3.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\com.fasterxml.jackson.core\jackson-databind\2.10.1\18eee15ffc662d27538d5b6ee84e4c92c0a9d03e\jackson-databind-2.10.1.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\com.google.code.findbugs\jsr305\3.0.2\25ea2e8b0c338a877313bd4672d3fe056ea78f0d\jsr305-3.0.2.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\org.jetbrains\annotations\16.0.1\c1a6655cebcac68e63e4c24d23f573035032eb2a\annotations-16.0.1.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\com.sedmelluq\lava-common\1.1.2\c9e2c5192a93847edd6b96c2f93530fdcda85028\lava-common-1.1.2.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\org.springframework.data\spring-data-commons\2.6.2\b86982771adfa51fdffbad50bed8ade75ac0710e\spring-data-commons-2.6.2.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\com.zaxxer\HikariCP\4.0.3\107cbdf0db6780a065f895ae9d8fbf3bb0e1c21f\HikariCP-4.0.3.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\org.apache.logging.log4j\log4j-to-slf4j\2.17.1\3619fd18278a1a895c1dca8c5be002768071a20e\log4j-to-slf4j-2.17.1.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\org.slf4j\jul-to-slf4j\1.7.36\ed46d81cef9c412a88caef405b58f93a678ff2ca\jul-to-slf4j-1.7.36.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\org.slf4j\slf4j-api\1.7.36\6c62681a2f655b49963a5983b8b0950a6120ae14\slf4j-api-1.7.36.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\com.neovisionaries\nv-websocket-client\2.14\1b66d37914b232cef45e56ec8c01907620de9e3a\nv-websocket-client-2.14.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\com.squareup.okhttp3\okhttp\3.13.0\f53f6362226e4546c3987b8693f3d6976df8c797\okhttp-3.13.0.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\club.minnced\opus-java\1.1.1\6656bf7d92e3bc058beb8f0f8a14fe9bc02da4b8\opus-java-1.1.1.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\org.apache.commons\commons-collections4\4.1\a4cf4688fe1c7e3a63aa636cc96d013af537768e\commons-collections4-4.1.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\ch.qos.logback\logback-core\1.2.10\5328406bfcae7bcdcc86810fcb2920d2c297170d\logback-core-1.2.10.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\org.apache.commons\commons-pool2\2.8.1\4f89525d29a81305edea6c504fd3b1ecf7ecd16e\commons-pool2-2.8.1.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\org.apache.httpcomponents\httpclient\4.5.10\7ca2e4276f4ef95e4db725a8cd4a1d1e7585b9e5\httpclient-4.5.10.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\commons-logging\commons-logging\1.2\4bfc12adfe4842bf07b657f0369c4cb522955686\commons-logging-1.2.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\dk.brics.automaton\automaton\1.11-8\6ebfa65eb431ff4b715a23be7a750cbc4cc96d0f\automaton-1.11-8.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\com.sedmelluq\lavaplayer-natives\1.3.13\ddbbad9bca297cd4a0b3e8ca42a9659712004566\lavaplayer-natives-1.3.13.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\commons-io\commons-io\2.6\815893df5f31da2ece4040fe0a12fd44b577afaf\commons-io-2.6.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\com.fasterxml.jackson.core\jackson-core\2.10.1\2c8b5e26ba40e5f91eb37a24075a2028b402c5f9\jackson-core-2.10.1.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\org.jsoup\jsoup\1.12.1\55819a28fc834c2f2bcf4dcdb278524dc3cf088f\jsoup-1.12.1.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\net.iharder\base64\2.3.9\6c34d1c85141be8a21b0a957d84359894e2684b7\base64-2.3.9.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\junit\junit\4.10\e4f1766ce7404a08f45d859fb9c226fc9e41a861\junit-4.10.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\org.springframework\spring-aop\5.3.16\d61c0545e0395de608be52db1cccb60ba841a26b\spring-aop-5.3.16.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\org.springframework\spring-orm\5.3.16\74e2e8227522a9f369b266b66fd71bf416d04a2f\spring-orm-5.3.16.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\org.springframework\spring-jdbc\5.3.16\456645153d6a5ed62b3d27750ee1b3c34b460b71\spring-jdbc-5.3.16.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\org.springframework\spring-tx\5.3.16\ae9be43379f2967e015ace189d47728a99f18220\spring-tx-5.3.16.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\org.springframework\spring-beans\5.3.16\15decec5cea7a91423272daaae6f5d050c23cf3b\spring-beans-5.3.16.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\org.springframework\spring-expression\5.3.16\831a17ce70686c571f3c05c4bcfb81012c5814df\spring-expression-5.3.16.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\org.springframework\spring-core\5.3.16\db1b277cd548c725144580dda8703ce179fb3769\spring-core-5.3.16.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\jakarta.transaction\jakarta.transaction-api\1.3.3\c4179d48720a1e87202115fbed6089bdc4195405\jakarta.transaction-api-1.3.3.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\jakarta.persistence\jakarta.persistence-api\2.2.3\8f6ea5daedc614f07a3654a455660145286f024e\jakarta.persistence-api-2.2.3.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\org.hibernate\hibernate-core\5.6.5.Final\d5c41d9018d16ca6076726f0f037ba8a0ebc0cb4\hibernate-core-5.6.5.Final.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\org.springframework\spring-aspects\5.3.16\499f2cd13d26fc0d6eb3312f0251c4f380300001\spring-aspects-5.3.16.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\org.checkerframework\checker-qual\3.5.0\2f50520c8abea66fbd8d26e481d3aef5c673b510\checker-qual-3.5.0.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\com.fasterxml.jackson.core\jackson-annotations\2.10.1\54d72475c0d6819f2d0e9a09d25c3ed876a4972f\jackson-annotations-2.10.1.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\com.squareup.okio\okio\1.17.2\78c7820b205002da4d2d137f6f312bd64b3d6049\okio-1.17.2.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\club.minnced\opus-java-api\1.1.1\63c6910fb1f7eebbfd922a01d37a4b94fa0ee5e3\opus-java-api-1.1.1.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\club.minnced\opus-java-natives\1.1.1\183af66d80cd508bbf45343f30ee280e0750f416\opus-java-natives-1.1.1.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\org.apache.httpcomponents\httpcore\4.4.12\21ebaf6d532bc350ba95bd81938fa5f0e511c132\httpcore-4.4.12.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\commons-codec\commons-codec\1.11\3acb4705652e16236558f0f4f2192cc33c3bd189\commons-codec-1.11.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\org.hamcrest\hamcrest-core\1.1\860340562250678d1a344907ac75754e259cdb14\hamcrest-core-1.1.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\org.springframework\spring-jcl\5.3.16\18d422952e0ce534c2b0ac8b47176c2432fb7e78\spring-jcl-5.3.16.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\org.aspectj\aspectjweaver\1.9.7\158f5c255cd3e4408e795b79f7c3fbae9b53b7ca\aspectjweaver-1.9.7.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\org.hibernate.common\hibernate-commons-annotations\5.1.2.Final\e59ffdbc6ad09eeb33507b39ffcf287679a498c8\hibernate-commons-annotations-5.1.2.Final.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\org.jboss.logging\jboss-logging\3.4.3.Final\c4bd7e12a745c0e7f6cf98c45cdcdf482fd827ea\jboss-logging-3.4.3.Final.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\net.bytebuddy\byte-buddy\1.12.7\e2a08a3fae74979ee76a8dd67cfd37958874e0fb\byte-buddy-1.12.7.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\antlr\antlr\2.7.7\83cd2cd674a217ade95a4bb83a8a14f351f48bd0\antlr-2.7.7.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\org.jboss\jandex\2.4.2.Final\1e1c385990b258ff1a24c801e84aebbacf70eb39\jandex-2.4.2.Final.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\com.fasterxml\classmate\1.5.1\3fe0bed568c62df5e89f4f174c101eab25345b6c\classmate-1.5.1.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\org.glassfish.jaxb\jaxb-runtime\2.3.1\dd6dda9da676a54c5b36ca2806ff95ee017d8738\jaxb-runtime-2.3.1.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\net.java.dev.jna\jna\4.4.0\cb208278274bf12ebdb56c61bd7407e6f774d65a\jna-4.4.0.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\jakarta.annotation\jakarta.annotation-api\1.3.5\59eb84ee0d616332ff44aba065f3888cf002cd2d\jakarta.annotation-api-1.3.5.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\org.yaml\snakeyaml\1.29\6d0cdafb2010f1297e574656551d7145240f6e25\snakeyaml-1.29.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\org.glassfish.jaxb\txw2\2.3.1\a09d2c48d3285f206fafbffe0e50619284e92126\txw2-2.3.1.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\com.sun.istack\istack-commons-runtime\3.0.7\c197c86ceec7318b1284bffb49b54226ca774003\istack-commons-runtime-3.0.7.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\org.jvnet.staxex\stax-ex\1.8\8cc35f73da321c29973191f2cf143d29d26a1df7\stax-ex-1.8.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\com.sun.xml.fastinfoset\FastInfoset\1.2.15\bb7b7ec0379982b97c62cd17465cb6d9155f68e8\FastInfoset-1.2.15.jar;C:\Users\Brock\.gradle\caches\modules-2\files-2.1\org.apache.logging.log4j\log4j-api\2.17.1\d771af8e336e372fb5399c99edabe0919aeaf5b2\log4j-api-2.17.1.jar com.wordpress.brancodes.main.Main


 */

//	public static void upsertCommands(Set<String> upsertCommands) {
//		Reactions.reactions.stream()
//				.filter(r -> upsertCommands.contains(r.getName()))
////				.filter(r -> r instanceof SlashCommand)
//				.map(r -> (SlashCommand) r)
//				.forEach(SlashCommand::upsertCommand);
//	}

	public static LiquidRichardBot getBot() {
		return bot;
	}

	public static void reset() {
		bot.pause();
		bot.getJDA().shutdownNow();
		bot.shutdownChatSchedulers();
	}

}
