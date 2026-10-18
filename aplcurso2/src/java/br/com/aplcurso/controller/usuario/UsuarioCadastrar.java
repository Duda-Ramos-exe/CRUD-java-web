
package br.com.aplcurso.controller.usuario;

import br.com.aplcurso.dao.UsuarioDAO;
import br.com.aplcurso.model.Usuario;
import br.com.aplcurso.utils.DocumentoValidador;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "UsuarioCadastrar", urlPatterns = {"/UsuarioCadastrar"})
public class UsuarioCadastrar extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/plain;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        try {
            String idTexto = valor(request, "id");
            String nome = valor(request, "nome");
            String cpf = valor(request, "cpf").replaceAll("\\D", "");
            String dataTexto = valor(request, "datanascimento");
            String email = valor(request, "email");
            String senha = valor(request, "senha");
            String salarioTexto = valor(request, "salario");

            int id = idTexto.isEmpty() ? 0 : Integer.parseInt(idTexto);

            if (id < 0 || nome.isEmpty() || cpf.isEmpty()
                    || dataTexto.isEmpty() || email.isEmpty()
                    || senha.isEmpty() || salarioTexto.isEmpty()) {
                resposta(response, "5");
                return;
            }

            // O banco atual permite apenas CPF com 11 dígitos.
            if (cpf.length() != 11
                    || !DocumentoValidador.isDocumentoValido(cpf)) {
                resposta(response, "3");
                return;
            }

            Date dataNascimento;
            try {
                dataNascimento = Date.valueOf(dataTexto);
            } catch (IllegalArgumentException ex) {
                resposta(response, "5");
                return;
            }

            // Aceita, por exemplo, 1234,56 ou R$ 1.234,56.
            String salarioNormalizado = salarioTexto
                    .replace("R$", "")
                    .replaceAll("\\s", "")
                    .replace(".", "")
                    .replace(",", ".");

            BigDecimal salario;
            try {
                salario = new BigDecimal(salarioNormalizado);
            } catch (NumberFormatException ex) {
                resposta(response, "5");
                return;
            }

            if (salario.signum() <= 0
                    || salario.compareTo(new BigDecimal("9999999999999.99")) > 0) {
                resposta(response, "5");
                return;
            }

            UsuarioDAO dao = new UsuarioDAO();

            // Impede duplicidade, mas permite manter o próprio CPF na edição.
            if (dao.cpfExiste(cpf, id)) {
                resposta(response, "4");
                return;
            }

            Usuario usuario = new Usuario();
            usuario.setId(id);
            usuario.setNome(nome.trim());
            usuario.setCpf(cpf);
            usuario.setDataNascimento(dataNascimento);
            usuario.setEmail(email.trim());
            usuario.setSenha(senha);
            usuario.setSalario(salario.doubleValue());

            if (dao.cadastrar(usuario)) {
                resposta(response, "1");
            } else {
                resposta(response, "0");
            }

        } catch (NumberFormatException ex) {
            resposta(response, "5");
        } catch (Exception ex) {
            getServletContext().log(
                    "Erro ao cadastrar/alterar usuário", ex);
            resposta(response, "0");
        }
    }

    private String valor(HttpServletRequest request, String nome) {
        String valor = request.getParameter(nome);
        return valor == null ? "" : valor.trim();
    }

    private void resposta(HttpServletResponse response, String codigo)
            throws IOException {
        response.getWriter().write(codigo);
    }
}
