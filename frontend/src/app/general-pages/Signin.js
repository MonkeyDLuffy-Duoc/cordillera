import React, { Component } from 'react'
import { withRouter } from 'react-router-dom'

export class Signin extends Component {
  constructor(props) {
    super(props);
    this.state = {
      username: '',
      password: '',
      error: '',
      loading: false
    };
  }

  handleInputChange = (event) => {
    const { name, value } = event.target;
    this.setState({ [name]: value, error: '' });
  }

  handleQuickLogin = (user, pass) => {
    this.setState({ username: user, password: pass, error: '' }, () => {
      this.handleSubmit(null);
    });
  }

  handleSubmit = async (event) => {
    if (event) event.preventDefault();
    this.setState({ loading: true, error: '' });

    try {
      const response = await fetch('http://localhost:8080/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          username: this.state.username,
          password: this.state.password
        })
      });

      const data = await response.json();

      if (response.ok) {
        localStorage.setItem('token', data.token);
        localStorage.setItem('username', data.username);
        localStorage.setItem('role', data.role);
        localStorage.setItem('nombreCompleto', data.nombreCompleto);
        localStorage.setItem('areaId', data.areaId || '');
        localStorage.setItem('equipoId', data.equipoId || '');

        this.props.history.push('/dashboard');
      } else {
        this.setState({ error: data || 'Error al iniciar sesión.', loading: false });
      }
    } catch (err) {
      this.setState({ error: 'No se pudo conectar al BFF. Habilita el backend.', loading: false });
    }
  }

  render() {
    return (
      <div>
        <div className="az-signin-wrapper" style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', backgroundColor: '#f4f5f8' }}>
          <div className="az-card-signin" style={{ width: '450px', padding: '40px', borderRadius: '12px', boxShadow: '0 8px 24px rgba(0,0,0,0.08)', backgroundColor: '#ffffff' }}>
            <h1 className="az-logo" style={{ textAlign: 'center', fontSize: '36px', color: '#5b47fb', marginBottom: '10px' }}>grupo<span>c</span>ordillera</h1>
            <div className="az-signin-header" style={{ marginTop: '20px' }}>
              <h2 style={{ fontSize: '22px', fontWeight: '600', color: '#1c273c', marginBottom: '8px' }}>¡Bienvenido de nuevo!</h2>
              <h4 style={{ fontSize: '14px', color: '#7987a1', fontWeight: '400', marginBottom: '24px' }}>Por favor, inicia sesión para continuar</h4>

              {this.state.error && (
                <div className="alert alert-danger" style={{ fontSize: '13px', borderRadius: '6px', padding: '10px 15px', marginBottom: '20px' }}>
                  {this.state.error}
                </div>
              )}

              <form onSubmit={this.handleSubmit}>
                <div className="form-group" style={{ marginBottom: '15px' }}>
                  <label style={{ fontSize: '12px', fontWeight: '600', color: '#495c74', textTransform: 'uppercase', letterSpacing: '0.5px' }}>Usuario</label>
                  <input 
                    type="text" 
                    name="username"
                    className="form-control" 
                    placeholder="Ingresa tu usuario" 
                    value={this.state.username}
                    onChange={this.handleInputChange}
                    required
                    style={{ borderRadius: '6px', padding: '10px 15px' }}
                  />
                </div>
                <div className="form-group" style={{ marginBottom: '25px' }}>
                  <label style={{ fontSize: '12px', fontWeight: '600', color: '#495c74', textTransform: 'uppercase', letterSpacing: '0.5px' }}>Contraseña</label>
                  <input 
                    type="password" 
                    name="password"
                    className="form-control" 
                    placeholder="Ingresa tu contraseña" 
                    value={this.state.password}
                    onChange={this.handleInputChange}
                    required
                    style={{ borderRadius: '6px', padding: '10px 15px' }}
                  />
                </div>
                <button type="submit" disabled={this.state.loading} className="btn btn-az-primary btn-block" style={{ borderRadius: '6px', padding: '12px', fontWeight: '600', backgroundColor: '#5b47fb', borderColor: '#5b47fb' }}>
                  {this.state.loading ? 'Iniciando sesión...' : 'Iniciar Sesión'}
                </button>
              </form>
            </div>

            <div style={{ marginTop: '30px', borderTop: '1px solid #e3e7ed', paddingTop: '20px' }}>
              <p style={{ fontSize: '12px', fontWeight: '600', color: '#495c74', textTransform: 'uppercase', marginBottom: '12px', letterSpacing: '0.5px', textAlign: 'center' }}>
                Acceso Rápido de Prueba (Demos)
              </p>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px' }}>
                <button 
                  onClick={() => this.handleQuickLogin('admin', 'admin123')}
                  className="btn btn-outline-light btn-sm"
                  style={{ fontSize: '11px', padding: '6px', textAlign: 'left', border: '1px solid #e3e7ed', color: '#1c273c' }}
                >
                  💼 <strong>Admin</strong><br/>admin / admin123
                </button>
                <button 
                  onClick={() => this.handleQuickLogin('gerente', 'gerente123')}
                  className="btn btn-outline-light btn-sm"
                  style={{ fontSize: '11px', padding: '6px', textAlign: 'left', border: '1px solid #e3e7ed', color: '#1c273c' }}
                >
                  📈 <strong>Gerente</strong><br/>gerente / gerente123
                </button>
                <button 
                  onClick={() => this.handleQuickLogin('jefe.ventas', 'jefe123')}
                  className="btn btn-outline-light btn-sm"
                  style={{ fontSize: '11px', padding: '6px', textAlign: 'left', border: '1px solid #e3e7ed', color: '#1c273c' }}
                >
                  👤 <strong>Jefe Ventas</strong><br/>jefe.ventas / jefe123
                </button>
                <button 
                  onClick={() => this.handleQuickLogin('juan.ventas', 'colab123')}
                  className="btn btn-outline-light btn-sm"
                  style={{ fontSize: '11px', padding: '6px', textAlign: 'left', border: '1px solid #e3e7ed', color: '#1c273c' }}
                >
                  🧑‍💻 <strong>Colaborador</strong><br/>juan.ventas / colab123
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    )
  }
}

export default withRouter(Signin)
