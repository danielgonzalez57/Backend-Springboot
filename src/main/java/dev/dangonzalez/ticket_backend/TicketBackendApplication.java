package dev.dangonzalez.ticket_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Equivalente al main.ts + AppModule de Nest, pero en un solo archivo.
// @SpringBootApplication es un "combo" de 3 anotaciones:
//   - @Configuration: esta clase puede declarar @Bean (como un provider en Nest)
//   - @EnableAutoConfiguration: Spring Boot escanea el classpath y configura solo
//     automáticamente (DataSource, Jackson, Security...) según las dependencias del pom.xml
//   - @ComponentScan: escanea este paquete y subpaquetes buscando @Component, @Service,
//     @Repository, @RestController... y los registra en el "contenedor" (el ApplicationContext,
//     que es el equivalente al contenedor de DI de Nest)
@SpringBootApplication
public class TicketBackendApplication {

	// Punto de entrada del programa, igual que bootstrap() en main.ts.
	// SpringApplication.run() levanta el ApplicationContext (contenedor de DI),
	// crea el servidor Tomcat embebido y arranca a escuchar peticiones.
	public static void main(String[] args) {
		SpringApplication.run(TicketBackendApplication.class, args);
	}

}
