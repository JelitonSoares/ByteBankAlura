package br.com.alura.bytebank.domain.conta;

import br.com.alura.bytebank.domain.cliente.Cliente;
import br.com.alura.bytebank.domain.cliente.DadosCadastroCliente;
import com.mysql.cj.x.protobuf.MysqlxPrepare;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class ContaDAO {

    private Connection conn;

    public ContaDAO(Connection conn) {
        this.conn = conn;
    }

    public void abrir(DadosAberturaConta dadosDaConta) {
        var cliente = new Cliente(dadosDaConta.dadosCliente());
        var conta = new Conta(dadosDaConta.numero(), BigDecimal.ZERO, cliente, true);

        String sql = "INSERT INTO conta (NUMERO, SALDO, CLIENTE_NOME, CLIENTE_CPF, CLIENTE_EMAIL, ATIVA) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1, conta.getNumero());
            ps.setBigDecimal(2, BigDecimal.ZERO);
            ps.setString(3, cliente.getNome());
            ps.setString(4, cliente.getCpf());
            ps.setString(5, cliente.getEmail());
            ps.setBoolean(6, conta.getAtiva());

            ps.execute();
            ps.close();


        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Set<Conta> listar() {
        String sql = "SELECT * FROM conta WHERE ativa = true;";
        Set<Conta> contas = new HashSet<>();

        try{
            PreparedStatement ps = conn.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                Integer numero = rs.getInt(1);
                BigDecimal saldo = rs.getBigDecimal(2);
                String nome = rs.getString(3);
                String cpf = rs.getString(4);
                String email = rs.getString(5);
                Boolean ativa = rs.getBoolean(6);

                DadosCadastroCliente dados = new DadosCadastroCliente(nome, cpf, email);

                contas.add(new Conta(numero, saldo, new Cliente(dados), ativa));
            }
            conn.close();
            ps.close();
            rs.close();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }

        return contas;
    }

    public Conta listarPorNumero(Integer numero) {
        String sql = "SELECT * FROM conta WHERE numero = ?;";
        Conta conta = null;

        try{
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, numero);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                BigDecimal saldo = rs.getBigDecimal(2);
                String nome = rs.getString(3);
                String cpf = rs.getString(4);
                String email = rs.getString(5);
                Boolean ativa = rs.getBoolean(6);

                DadosCadastroCliente dados = new DadosCadastroCliente(nome, cpf, email);
                conta = new Conta(numero, saldo, new Cliente(dados), ativa);
            }

            conn.close();
            ps.close();
            rs.close();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }

        return conta;
    }

    public void atualizar(Integer numero, BigDecimal valor) {
        String sql = "UPDATE conta SET saldo = ? WHERE numero = ?;";

        try{
            PreparedStatement ps = conn.prepareStatement(sql);
            conn.setAutoCommit(false);

            ps.setBigDecimal(1, valor);
            ps.setInt(2, numero);
            ps.execute();
            conn.commit();

            ps.close();
            conn.close();
        }catch (SQLException e) {
            try{
                conn.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex.getMessage());
            }
            throw new RuntimeException(e.getMessage());
        }
    }

    public void excluir(Integer numeroDaConta){
        String sql = "DELETE FROM conta WHERE numero = ?;";

        try{
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, numeroDaConta);

            ps.execute();
            ps.close();
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void encerrar(Integer numeroDaConta) {
        String sql = "UPDATE conta SET ativa = false WHERE numero = ?;";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, numeroDaConta);

            ps.execute();
            ps.close();
            conn.close();
        }catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
