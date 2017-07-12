package com.mesilat.certs;

import java.util.Date;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

@Path("/cert")
public class CheckCertResource {
    private static final long MS = 24l * 3600l * 1000l;

    @Path("notAfter")
    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getNotAfter(@QueryParam("host") String host, @QueryParam("port") Integer port) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            CheckCertificate check = new CheckCertificateImpl();
            Date notAfter = check.getNotAfter(host, port == null? 443: port);
            ObjectNode node = mapper.createObjectNode();
            node.put("expires", notAfter.toString());
            node.put("days", (notAfter.getTime()-System.currentTimeMillis()) / MS);
            return Response.ok(node.toString()).build();
        } catch (CheckCertificateException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.toString()).build();
        }
    }
}