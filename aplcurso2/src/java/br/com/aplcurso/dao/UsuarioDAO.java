
package br.com.aplcurso.dao;

import br.com.aplcurso.model.Usuario;
import br.com.aplcurso.utils.SingleConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO implements GenericDAO {

    private final Connection conexao;

    public UsuarioDAO() throws Exception {
        conexao = SingleConnection.getConnection();

        if (conexao == null) {
            throw new SQLException("Conexão com o banco indisponível.");
        }
    }

    @Override
    public Boolean cadastrar(Object objeto) {
        Usuario usuario = (Usuario) objeto;

        if (usuario.getId() == 0) {
            return inserir(usuario);
        }

        return alterar(usuario);
    }

    @Override
    public Boolean inserir(Object objeto) {
        Usuario usuario = (Usuario) objeto;

        String sql = "INSERT INTO usuario "
                + "(nome, datanascimento, cpf, email, senha, salario) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, usuario.getNome());
            stmt.setDate(2, new java.sql.Date(
                    usuario.getDataNascimento().getTime()));
            stmt.setString(3, usuario.getCpf());
            stmt.setString(4, usuario.getEmail());
            stmt.setString(5, usuario.getSenha());
            stmt.setDouble(6, usuario.getSalario());

            int linhas = stmt.executeUpdate();

            if (linhas > 0) {
                conexao.commit();
                return true;
            }

            conexao.rollback();
            return false;

        } catch (SQLException ex) {
            rollback();
            System.err.println("Erro ao inserir usuário: "
                    + ex.getMessage());
            return false;
        }
    }

    @Override
    public Boolean alterar(Object objeto) {
        Usuario usuario = (Usuario) objeto;

        String sql = "UPDATE usuario SET "
                + "nome = ?, datanascimento = ?, cpf = ?, "
                + "email = ?, senha = ?, salario = ? "
                + "WHERE id = ?";

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, usuario.getNome());
            stmt.setDate(2, new java.sql.Date(
                    usuario.getDataNascimento().getTime()));
            stmt.setString(3, usuario.getCpf());
            stmt.setString(4, usuario.getEmail());
            stmt.setString(5, usuario.getSenha());
            stmt.setDouble(6, usuario.getSalario());
            stmt.setInt(7, usuario.getId());

            int linhas = stmt.executeUpdate();

            if (linhas > 0) {
                conexao.commit();
                return true;
            }

            conexao.rollback();
            return false;

        } catch (SQLException ex) {
            rollback();
            System.err.println("Erro ao alterar usuário: "
                    + ex.getMessage());
            return false;
        }
    }

    
@Override
public Boolean excluir(int id) {
    String sql = "DELETE FROM usuario WHERE id = ?";

    try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
        stmt.setInt(1, id);

        System.out.println("Tentando excluir usuário ID: " + id);

        int linhas = stmt.executeUpdate();

        System.out.println("Linhas excluídas: " + linhas);

        if (linhas > 0) {
            conexao.commit();
            System.out.println("Exclusão confirmada.");
            return true;
        }

        conexao.rollback();
        System.out.println("Nenhum usuário encontrado com ID: " + id);
        return false;

    } catch (SQLException ex) {
        rollback();
        System.err.println("Erro SQL ao excluir usuário:");
        ex.printStackTrace();
        return false;
    }
}


    @Override
    public Object carregar(int id) {
        String sql = "SELECT * FROM usuario WHERE id = ?";

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return preencherUsuario(rs);
                }
            }

        } catch (SQLException ex) {
            System.err.println("Erro ao carregar usuário: "
                    + ex.getMessage());
        }

        return null;
    }

    @Override
    public List<Object> listar() {
        List<Object> resultado = new ArrayList<>();
        String sql = "SELECT * FROM usuario ORDER BY id";

        try (PreparedStatement stmt = conexao.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                resultado.add(preencherUsuario(rs));
            }

        } catch (SQLException ex) {
            System.err.println("Erro ao listar usuários: "
                    + ex.getMessage());
        }

        return resultado;
    }

    public boolean cpfExiste(String cpf) {
        return cpfExiste(cpf, 0);
    }

    public boolean cpfExiste(String cpf, int idAtual) {
        String sql = "SELECT 1 FROM usuario "
                + "WHERE cpf = ? AND id <> ? LIMIT 1";

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, cpf);
            stmt.setInt(2, idAtual);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException ex) {
            throw new RuntimeException(
                    "Erro ao verificar CPF no banco de dados.", ex);
        }
    }

    private Usuario preencherUsuario(ResultSet rs)
            throws SQLException {

        Usuario usuario = new Usuario();

        usuario.setId(rs.getInt("id"));
        usuario.setNome(rs.getString("nome"));
        usuario.setCpf(rs.getString("cpf"));
        usuario.setEmail(rs.getString("email"));
        usuario.setSalario(rs.getDouble("salario"));
        usuario.setDataNascimento(rs.getDate("datanascimento"));

        // O modelo atual não possui um objeto Estado.
        return usuario;
    }

    private void rollback() {
        try {
            conexao.rollback();
        } catch (SQLException ex) {
            System.err.println("Erro ao desfazer transação: "
                    + ex.getMessage());
        }
    }
}
