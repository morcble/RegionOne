import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Date;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

public class JwtTest {

	public static void main(String[] args) throws IllegalArgumentException, UnsupportedEncodingException {
		String a ="<div class=\"calendar\">\r\n" + 
				"		<div class=\"header\">\r\n" + 
				"			<div class=\"date-block\">\r\n" + 
				"				<i class=\"pre-year fa fa-angle-left\"></i>\r\n" + 
				"				<div class=\"year\">	\r\n" + 
				"				</div>\r\n" + 
				"				<i class=\"next-year fa fa-angle-right\"></i>\r\n" + 
				"			</div>\r\n" + 
				"			\r\n" + 
				"			<div class=\"date-block\">\r\n" + 
				"				<i class=\"pre-month fa fa-angle-left\"></i>\r\n" + 
				"				<div class=\"month\">\r\n" + 
				"				</div>\r\n" + 
				"				<i class=\"next-month fa fa-angle-right\"></i>\r\n" + 
				"			</div>\r\n" + 
				"		</div>\r\n" + 
				"		<div class=\"spliter\"></div>\r\n" + 
				"		<div class=\"body\"></div>\r\n" + 
				"		<div class=\"spliter\"></div>\r\n" + 
				"		<div class=\"footer\">\r\n" + 
				"			<span class=\"today-btn\" msgKey=\"global_msg.today\"></span>\r\n" + 
				"			<span msgKey=\"global_msg.ok\"></span>\r\n" + 
				"		</div>\r\n" + 
				"	</div>";
		
		String reqURI = "/MorcbleLocalService/region/common/file/view";
		int regionTagIndex = reqURI.indexOf("/region/");
		System.out.println(reqURI.substring(regionTagIndex + 7)); 
		
		 Algorithm algorithm = Algorithm.HMAC256("OPHgw^hm!@#mm,%&*h*DF&sd&*GH_+gj=HJ");
		 Algorithm algorithm1 = Algorithm.HMAC256("OPHgw^hm!@#gg,%&*h*DF&sd&*GH_+gj=HJ");
		 
		 Date now = new Date();
		 Date expireDt = new Date();
		 String token = JWT.create()
		        .withIssuer("regionsoft")
		        .withIssuedAt(now)
		        .withExpiresAt(expireDt)
		        .withClaim("123", "1233")
		        .sign(algorithm);
		 System.out.println(token);
		 try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 JWTVerifier verifier1 = JWT.require(algorithm)
				
				 .build();
		 DecodedJWT decodedJWT = verifier1.verify(token);
		 System.out.println(decodedJWT.getClaim("123").asString());
		 
		// verifier1.verify(token);
		// verifier1.verify(token1);
		 
		// System.out.println( new String(Base64.getDecoder().decode("eyJpc3MiOiJhdXRoMDIyIiwiaWF0IjoxNTEzNTYxNzMwfQ")));
		 /**
		 eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJhdXRoMDIyIn0.0N0wGsh2Q-zZ0P81Yw81RmPDmtWon1JJdbO4Yh1c2S4
		  */
		/* DecodedJWT decodedJWT = verifier1.verify(token);
		 String a = decodedJWT.getIssuer();
	
		    System.out.println(decodedJWT.getIssuer());
		    System.out.println(decodedJWT.getIssuedAt());
		    System.out.println(decodedJWT.getExpiresAt());
		    System.out.println(decodedJWT.getToken());
		    
		    */
		    
	}

}
