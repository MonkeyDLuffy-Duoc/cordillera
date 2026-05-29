import React, { Component } from 'react'
import { Line } from 'react-chartjs-2';

export class Dashboard extends Component {
  constructor(props) {
    super(props);
    this.state = {
      username: '',
      role: '',
      nombreCompleto: '',
      areas: [],
      equipos: [],
      kpis: [],
      metasReporte: [],
      medicionesHistoricas: [],
      selectedKpiId: 1, // Default selected KPI for the trend chart
      loading: true,
      error: '',
      // Form state for creating a new Goal (Metas)
      newMetaKpiId: '',
      newMetaEquipoId: '',
      newMetaValor: '',
      newMetaFecha: '',
      newMetaSuccess: '',
      newMetaError: '',
      newMetaLoading: false,
      // New Admin Tab State
      activeTab: 'dashboard', // 'dashboard' or 'users'
      usuariosList: [],
      usuariosLoading: false,
      usuariosError: '',
      editingUserUsername: null, // Track which user is currently being edited
      // Form state for creating a new User
      newUsrUsername: '',
      newUsrPassword: '',
      newUsrNombreCompleto: '',
      newUsrRole: 'COLABORADOR',
      newUsrAreaId: '',
      newUsrEquipoId: '',
      newUsrSuccess: '',
      newUsrError: '',
      newUsrSubmitLoading: false,
      // Goals Editing Inline State
      editingMetaId: null,
      editMetaValor: '',
      editMetaFecha: '2026-06-30',
      editMetaEstado: 'ACTIVA'
    };
  }

  componentDidMount() {
    this.fetchDashboardData();
  }

  fetchDashboardData = async () => {
    const token = localStorage.getItem('token');
    const nombreCompleto = localStorage.getItem('nombreCompleto') || 'Usuario Demo';
    
    if (!token) {
      this.props.history.push('/general-pages/signin');
      return;
    }

    try {
      const response = await fetch('http://localhost:8080/api/bff/dashboard', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      if (response.ok) {
        const data = await response.json();
        this.setState({
          username: data.username,
          role: data.role,
          nombreCompleto: nombreCompleto,
          areas: data.areas || [],
          equipos: data.equipos || [],
          kpis: data.kpis || [],
          metasReporte: data.metasReporte || [],
          medicionesHistoricas: data.medicionesHistoricas || [],
          loading: false,
          // Set default form selections
          newMetaKpiId: data.kpis.length > 0 ? data.kpis[0].id : '',
          newMetaEquipoId: data.equipos.length > 0 ? data.equipos[0].id : '',
          newUsrAreaId: data.areas.length > 0 ? data.areas[0].id : '',
          newUsrEquipoId: data.equipos.length > 0 ? data.equipos[0].id : ''
        });
        
        // If they are Admin, preemptively fetch the user list
        if (data.role === 'ADMIN') {
          this.fetchUsuarios();
        }
      } else {
        this.setState({ error: 'Error al cargar los datos del BFF.', loading: false });
      }
    } catch (err) {
      this.setState({ error: 'No se pudo conectar al BFF Gateway. Asegúrate de levantar los servicios.', loading: false });
    }
  }

  fetchUsuarios = async () => {
    this.setState({ usuariosLoading: true, usuariosError: '' });
    const token = localStorage.getItem('token');
    try {
      const response = await fetch('http://localhost:8080/api/bff/usuarios', {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (response.ok) {
        const data = await response.json();
        this.setState({ usuariosList: data || [], usuariosLoading: false });
      } else {
        this.setState({ usuariosError: 'Error al cargar la lista de usuarios.', usuariosLoading: false });
      }
    } catch (err) {
      this.setState({ usuariosError: 'Error de red al cargar usuarios.', usuariosLoading: false });
    }
  }

  handleCreateUsuario = async (e) => {
    e.preventDefault();
    if (this.state.editingUserUsername) {
      this.handleUpdateUsuario(e);
      return;
    }
    this.setState({ newUsrSubmitLoading: true, newUsrError: '', newUsrSuccess: '' });
    const token = localStorage.getItem('token');

    const areaIdVal = this.state.newUsrAreaId ? parseInt(this.state.newUsrAreaId) : null;
    const equipoIdVal = this.state.newUsrEquipoId ? parseInt(this.state.newUsrEquipoId) : null;

    try {
      const response = await fetch('http://localhost:8080/api/bff/usuarios', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
          username: this.state.newUsrUsername,
          password: this.state.newUsrPassword,
          nombreCompleto: this.state.newUsrNombreCompleto,
          role: this.state.newUsrRole,
          areaId: areaIdVal,
          equipoId: equipoIdVal
        })
      });

      const data = await response.text();

      if (response.ok) {
        this.setState({
          newUsrSuccess: '¡Usuario creado con éxito en MySQL!',
          newUsrUsername: '',
          newUsrPassword: '',
          newUsrNombreCompleto: '',
          newUsrRole: 'COLABORADOR',
          newUsrAreaId: '',
          newUsrEquipoId: '',
          newUsrSubmitLoading: false
        });
        this.fetchUsuarios();
      } else {
        this.setState({ newUsrError: data || 'Error al crear el usuario.', newUsrSubmitLoading: false });
      }
    } catch (err) {
      this.setState({ newUsrError: 'Error de red al intentar crear el usuario.', newUsrSubmitLoading: false });
    }
  }

  handleUpdateUsuario = async (e) => {
    e.preventDefault();
    this.setState({ newUsrSubmitLoading: true, newUsrError: '', newUsrSuccess: '' });
    const token = localStorage.getItem('token');

    const areaIdVal = this.state.newUsrAreaId ? parseInt(this.state.newUsrAreaId) : null;
    const equipoIdVal = this.state.newUsrEquipoId ? parseInt(this.state.newUsrEquipoId) : null;

    try {
      const response = await fetch(`http://localhost:8080/api/bff/usuarios/${this.state.editingUserUsername}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
          username: this.state.editingUserUsername,
          password: this.state.newUsrPassword,
          nombreCompleto: this.state.newUsrNombreCompleto,
          role: this.state.newUsrRole,
          areaId: areaIdVal,
          equipoId: equipoIdVal
        })
      });

      const data = await response.text();

      if (response.ok) {
        this.setState({
          newUsrSuccess: '¡Usuario actualizado con éxito en MySQL!',
          editingUserUsername: null,
          newUsrUsername: '',
          newUsrPassword: '',
          newUsrNombreCompleto: '',
          newUsrRole: 'COLABORADOR',
          newUsrAreaId: '',
          newUsrEquipoId: '',
          newUsrSubmitLoading: false
        });
        this.fetchUsuarios();
      } else {
        this.setState({ newUsrError: data || 'Error al actualizar el usuario.', newUsrSubmitLoading: false });
      }
    } catch (err) {
      this.setState({ newUsrError: 'Error de red al intentar actualizar el usuario.', newUsrSubmitLoading: false });
    }
  }

  handleEditUsuarioClick = (user) => {
    this.setState({
      editingUserUsername: user.username,
      newUsrUsername: user.username,
      newUsrNombreCompleto: user.nombreCompleto,
      newUsrPassword: '', // empty to keep original
      newUsrRole: user.role,
      newUsrAreaId: user.areaId || '',
      newUsrEquipoId: user.equipoId || '',
      newUsrSuccess: '',
      newUsrError: ''
    });
  }

  handleCancelEditUsuario = () => {
    this.setState({
      editingUserUsername: null,
      newUsrUsername: '',
      newUsrNombreCompleto: '',
      newUsrPassword: '',
      newUsrRole: 'COLABORADOR',
      newUsrAreaId: '',
      newUsrEquipoId: '',
      newUsrSuccess: '',
      newUsrError: ''
    });
  }

  handleDeleteUsuario = async (username) => {
    if (!window.confirm(`¿Estás seguro de que deseas eliminar al usuario "${username}"?`)) {
      return;
    }
    const token = localStorage.getItem('token');
    try {
      const response = await fetch(`http://localhost:8080/api/bff/usuarios/${username}`, {
        method: 'DELETE',
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (response.ok) {
        this.fetchUsuarios();
      } else {
        alert('Error al eliminar el usuario.');
      }
    } catch (err) {
      alert('Error de red al intentar eliminar.');
    }
  }

  handleKpiSelectorChange = (event) => {
    this.setState({ selectedKpiId: parseInt(event.target.value) });
  }

  handleInputChange = (event) => {
    const { name, value } = event.target;
    this.setState({ [name]: value, newMetaError: '', newMetaSuccess: '', newUsrError: '', newUsrSuccess: '' });
  }

  handleCreateMeta = async (event) => {
    event.preventDefault();
    this.setState({ newMetaLoading: true, newMetaError: '', newMetaSuccess: '' });

    const token = localStorage.getItem('token');

    try {
      const response = await fetch('http://localhost:8080/api/bff/metas', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
          kpiId: parseInt(this.state.newMetaKpiId),
          equipoId: parseInt(this.state.newMetaEquipoId),
          valorObjetivo: parseFloat(this.state.newMetaValor),
          fechaLimite: this.state.newMetaFecha,
          estado: 'ACTIVA'
        })
      });

      const data = await response.text();

      if (response.ok) {
        this.setState({
          newMetaSuccess: '¡Meta creada con éxito!',
          newMetaValor: '',
          newMetaFecha: '',
          newMetaLoading: false
        });
        // Reload dashboard data
        this.fetchDashboardData();
      } else {
        this.setState({ newMetaError: data || 'Error al crear la meta.', newMetaLoading: false });
      }
    } catch (err) {
      this.setState({ newMetaError: 'Error de red al intentar crear la meta.', newMetaLoading: false });
    }
  }

  handleDeleteMeta = async (id) => {
    if (!window.confirm('¿Estás seguro de que deseas eliminar esta meta?')) {
      return;
    }
    const token = localStorage.getItem('token');
    try {
      const response = await fetch(`http://localhost:8080/api/bff/metas/${id}`, {
        method: 'DELETE',
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (response.ok) {
        this.fetchDashboardData();
      } else {
        alert('Error al eliminar la meta.');
      }
    } catch (err) {
      alert('Error de red al eliminar la meta.');
    }
  }

  startEditingMeta = (meta) => {
    this.setState({
      editingMetaId: meta.id,
      editMetaValor: meta.valorObjetivo,
      editMetaFecha: '2026-06-30',
      editMetaEstado: meta.estado
    });
  }

  cancelEditingMeta = () => {
    this.setState({ editingMetaId: null });
  }

  handleUpdateMeta = async (id) => {
    const token = localStorage.getItem('token');
    const originalMeta = this.state.metasReporte.find(m => m.id === id);
    const kpi = this.state.kpis.find(k => k.nombre === originalMeta.kpiNombre);
    const equipo = this.state.equipos.find(eq => eq.nombre === originalMeta.equipoNombre);

    try {
      const response = await fetch(`http://localhost:8080/api/bff/metas/${id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
          kpiId: kpi ? kpi.id : 1,
          equipoId: equipo ? equipo.id : 1,
          valorObjetivo: parseFloat(this.state.editMetaValor),
          fechaLimite: this.state.editMetaFecha,
          estado: this.state.editMetaEstado
        })
      });

      if (response.ok) {
        this.setState({ editingMetaId: null });
        this.fetchDashboardData();
      } else {
        alert('Error al actualizar la meta.');
      }
    } catch (err) {
      alert('Error de red al actualizar la meta.');
    }
  }

  getChartDataForSelectedKpi = () => {
    const { selectedKpiId, medicionesHistoricas, kpis } = this.state;
    
    const selectedKpi = kpis.find(k => k.id === selectedKpiId);
    if (!selectedKpi) return { labels: [], datasets: [] };

    // Filter measurements for this KPI and sort by date
    const kpiMediciones = medicionesHistoricas
      .filter(m => m.kpiId === selectedKpiId)
      .sort((a, b) => new Date(a.fechaRegistro) - new Date(b.fechaRegistro));

    const labels = kpiMediciones.map(m => {
      const date = new Date(m.fechaRegistro);
      return date.toLocaleDateString('es-ES', { month: 'short', year: 'numeric' });
    });
    
    const dataValues = kpiMediciones.map(m => m.valor);

    return {
      labels: labels,
      datasets: [{
        label: `${selectedKpi.nombre} (${selectedKpi.unidadMedida})`,
        data: dataValues,
        borderWidth: 3,
        fill: true,
        backgroundColor: 'rgba(91, 71, 251, 0.05)',
        borderColor: '#5b47fb',
        pointBackgroundColor: '#5b47fb',
        pointBorderColor: '#ffffff',
        pointRadius: 5,
        pointHoverRadius: 7,
        lineTension: 0.3
      }]
    };
  }

  render() {
    const { 
      loading, error, role, nombreCompleto, areas, equipos, kpis, metasReporte, selectedKpiId, activeTab 
    } = this.state;

    if (loading) {
      return (
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', height: '80vh' }}>
          <div className="spinner-border text-primary" role="status" style={{ width: '3rem', height: '3rem' }}>
            <span className="sr-only">Cargando...</span>
          </div>
          <p style={{ marginTop: '15px', color: '#7987a1', fontWeight: '500' }}>Cargando panel de control inteligente...</p>
        </div>
      );
    }

    if (error) {
      return (
        <div className="container" style={{ marginTop: '50px' }}>
          <div className="alert alert-danger" role="alert" style={{ borderRadius: '8px', padding: '20px' }}>
            <h4 className="alert-heading">⚠️ Error de Conexión</h4>
            <p>{error}</p>
            <hr />
            <p className="mb-0" style={{ fontSize: '13px' }}>
              Por favor, asegúrate de iniciar los microservicios de Spring Boot (Eureka, BFF, KPIs, Metas, Áreas) en tu entorno local.
            </p>
          </div>
        </div>
      );
    }

    const selectedKpi = kpis.find(k => k.id === selectedKpiId);
    const chartData = this.getChartDataForSelectedKpi();

    // Calculate overall compliance rate
    const totalGoals = metasReporte.length;
    const averageCompliance = totalGoals > 0 
      ? Math.round(metasReporte.reduce((acc, m) => acc + m.cumplimiento, 0) / totalGoals * 10.0) / 10.0
      : 0;

    return (
      <div className="container p-md-0" style={{ marginTop: '30px', paddingBottom: '60px' }}>
        <div className="az-content-body">
          {/* Dashboard Header */}
          <div className="az-dashboard-one-title" style={{ borderBottom: 'none', marginBottom: '25px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap' }}>
            <div>
              <h2 className="az-dashboard-title" style={{ fontSize: '26px', fontWeight: '700', color: '#1c273c' }}>
                ¡Hola, {nombreCompleto}!
              </h2>
              <p className="az-dashboard-text" style={{ fontSize: '14px', color: '#7987a1' }}>
                Panel de Monitoreo Inteligente de KPIs de Grupo Cordillera
              </p>
            </div>
            <div style={{ display: 'flex', gap: '10px' }}>
              <span className="badge" style={{ backgroundColor: '#eef2ff', color: '#5b47fb', fontSize: '12px', padding: '8px 16px', borderRadius: '20px', fontWeight: '600' }}>
                💼 ROL: {role}
              </span>
            </div>
          </div>

          {/* Admin Tabs System */}
          {role === 'ADMIN' && (
            <div className="mg-b-25" style={{ borderBottom: '1px solid #f3f5f7', paddingBottom: '5px' }}>
              <ul className="nav nav-tabs" style={{ backgroundColor: 'transparent', borderBottom: 'none', display: 'flex', gap: '10px' }}>
                <li className="nav-item">
                  <button 
                    onClick={() => this.setState({ activeTab: 'dashboard' })}
                    className={`nav-link btn ${activeTab === 'dashboard' ? 'active font-weight-bold text-primary' : 'text-muted'}`}
                    style={{ border: 'none', outline: 'none', background: 'none', fontSize: '14px', padding: '8px 20px', borderRadius: '20px', fontWeight: '600', backgroundColor: activeTab === 'dashboard' ? '#eef2ff' : 'transparent', color: activeTab === 'dashboard' ? '#5b47fb' : '#64748b' }}
                  >
                    📊 KPIs y Objetivos
                  </button>
                </li>
                <li className="nav-item">
                  <button 
                    onClick={() => this.setState({ activeTab: 'users' })}
                    className={`nav-link btn ${activeTab === 'users' ? 'active font-weight-bold text-primary' : 'text-muted'}`}
                    style={{ border: 'none', outline: 'none', background: 'none', fontSize: '14px', padding: '8px 20px', borderRadius: '20px', fontWeight: '600', backgroundColor: activeTab === 'users' ? '#eef2ff' : 'transparent', color: activeTab === 'users' ? '#5b47fb' : '#64748b' }}
                  >
                    👥 Gestión de Usuarios (MySQL)
                  </button>
                </li>
              </ul>
            </div>
          )}

          {activeTab === 'dashboard' ? (
            <>
              {/* Quick Metrics Cards */}
              <div className="row row-sm mg-b-20">
                <div className="col-sm-6 col-lg-3">
                  <div className="card card-dashboard-two" style={{ border: 'none', borderRadius: '10px', boxShadow: '0 4px 12px rgba(0,0,0,0.03)', padding: '20px' }}>
                    <div className="card-header" style={{ padding: '0', border: 'none', backgroundColor: 'transparent' }}>
                      <h6 style={{ fontSize: '28px', fontWeight: '700', color: '#1c273c', margin: '0' }}>{areas.length}</h6>
                      <p style={{ color: '#7987a1', fontSize: '12px', fontWeight: '600', textTransform: 'uppercase', marginTop: '5px', marginBottom: '0' }}>
                        🏢 Áreas Activas
                      </p>
                    </div>
                  </div>
                </div>
                <div className="col-sm-6 col-lg-3 mg-t-20 mg-sm-t-0">
                  <div className="card card-dashboard-two" style={{ border: 'none', borderRadius: '10px', boxShadow: '0 4px 12px rgba(0,0,0,0.03)', padding: '20px' }}>
                    <div className="card-header" style={{ padding: '0', border: 'none', backgroundColor: 'transparent' }}>
                      <h6 style={{ fontSize: '28px', fontWeight: '700', color: '#1c273c', margin: '0' }}>{equipos.length}</h6>
                      <p style={{ color: '#7987a1', fontSize: '12px', fontWeight: '600', textTransform: 'uppercase', marginTop: '5px', marginBottom: '0' }}>
                        👥 Equipos Monitoreados
                      </p>
                    </div>
                  </div>
                </div>
                <div className="col-sm-6 col-lg-3 mg-t-20 mg-lg-t-0">
                  <div className="card card-dashboard-two" style={{ border: 'none', borderRadius: '10px', boxShadow: '0 4px 12px rgba(0,0,0,0.03)', padding: '20px' }}>
                    <div className="card-header" style={{ padding: '0', border: 'none', backgroundColor: 'transparent' }}>
                      <h6 style={{ fontSize: '28px', fontWeight: '700', color: '#1c273c', margin: '0' }}>{kpis.length}</h6>
                      <p style={{ color: '#7987a1', fontSize: '12px', fontWeight: '600', textTransform: 'uppercase', marginTop: '5px', marginBottom: '0' }}>
                        📊 Indicadores (KPIs)
                      </p>
                    </div>
                  </div>
                </div>
                <div className="col-sm-6 col-lg-3 mg-t-20 mg-lg-t-0">
                  <div className="card card-dashboard-two" style={{ border: 'none', borderRadius: '10px', boxShadow: '0 4px 12px rgba(0,0,0,0.03)', padding: '20px', backgroundColor: '#eef2ff' }}>
                    <div className="card-header" style={{ padding: '0', border: 'none', backgroundColor: 'transparent' }}>
                      <h6 style={{ fontSize: '28px', fontWeight: '700', color: '#5b47fb', margin: '0' }}>{averageCompliance}%</h6>
                      <p style={{ color: '#5b47fb', fontSize: '12px', fontWeight: '600', textTransform: 'uppercase', marginTop: '5px', marginBottom: '0' }}>
                        🎯 Cumplimiento Promedio
                      </p>
                    </div>
                  </div>
                </div>
              </div>

              {/* Main Dashboard Layout */}
              <div className="row row-sm mg-b-20">
                {/* Historical trend Chart */}
                <div className="col-lg-8 ht-lg-100p">
                  <div className="card card-dashboard-one" style={{ border: 'none', borderRadius: '10px', boxShadow: '0 4px 12px rgba(0,0,0,0.03)', padding: '20px' }}>
                    <div className="card-header" style={{ border: 'none', padding: '0 0 20px 0', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', backgroundColor: 'transparent' }}>
                      <div>
                        <h6 className="card-title" style={{ fontSize: '16px', fontWeight: '700', color: '#1c273c', margin: '0' }}>
                          Tendencia Histórica de KPIs
                        </h6>
                        <p className="card-text" style={{ fontSize: '13px', color: '#7987a1', marginTop: '3px' }}>
                          Visualiza el comportamiento de los indicadores clave a lo largo del tiempo.
                        </p>
                      </div>
                      <div>
                        <select 
                          className="form-control" 
                          value={selectedKpiId}
                          onChange={this.handleKpiSelectorChange}
                          style={{ borderRadius: '6px', minWidth: '220px', fontSize: '13px' }}
                        >
                          {kpis.map(k => (
                            <option key={k.id} value={k.id}>{k.nombre}</option>
                          ))}
                        </select>
                      </div>
                    </div>

                    <div className="card-body" style={{ padding: '0' }}>
                      {selectedKpi && (
                        <div style={{ padding: '10px 15px', backgroundColor: '#f8fafc', borderRadius: '8px', marginBottom: '20px' }}>
                          <span style={{ fontSize: '11px', fontWeight: '700', color: '#64748b', textTransform: 'uppercase', letterSpacing: '0.5px' }}>Descripción:</span>
                          <p style={{ margin: '3px 0 0 0', fontSize: '13px', color: '#1e293b' }}>{selectedKpi.descripcion}</p>
                        </div>
                      )}
                      <div className="page-view-chart-wrapper" style={{ height: '280px', position: 'relative' }}>
                        {chartData.labels && chartData.labels.length > 0 ? (
                          <Line 
                            data={chartData} 
                            options={{
                              maintainAspectRatio: false,
                              responsive: true,
                              legend: { display: false },
                              scales: {
                                yAxes: [{
                                  ticks: { fontColor: '#64748b', fontSize: 11 }
                                }],
                                xAxes: [{
                                  ticks: { fontColor: '#64748b', fontSize: 11 }
                                }]
                              }
                            }} 
                          />
                        ) : (
                          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%', color: '#94a3b8' }}>
                            No hay mediciones registradas para este KPI.
                          </div>
                        )}
                      </div>
                    </div>
                  </div>
                </div>

                {/* Create Goal Form (Role Protected: only Admin and Jefe de Área) */}
                <div className="col-lg-4 mg-t-20 mg-lg-t-0">
                  {['ADMIN', 'JEFE_AREA'].includes(role) ? (
                    <div className="card" style={{ border: 'none', borderRadius: '10px', boxShadow: '0 4px 12px rgba(0,0,0,0.03)', padding: '20px', height: '100%' }}>
                      <h6 style={{ fontSize: '16px', fontWeight: '700', color: '#1c273c', marginBottom: '15px' }}>
                        🎯 Definir Nueva Meta
                      </h6>
                      
                      {this.state.newMetaSuccess && (
                        <div className="alert alert-success" style={{ fontSize: '12px', padding: '8px 12px', borderRadius: '6px' }}>
                          {this.state.newMetaSuccess}
                        </div>
                      )}
                      {this.state.newMetaError && (
                        <div className="alert alert-danger" style={{ fontSize: '12px', padding: '8px 12px', borderRadius: '6px' }}>
                          {this.state.newMetaError}
                        </div>
                      )}

                      <form onSubmit={this.handleCreateMeta}>
                        <div className="form-group" style={{ marginBottom: '12px' }}>
                          <label style={{ fontSize: '11px', fontWeight: '600', color: '#64748b' }}>KPI Asociado</label>
                          <select 
                            name="newMetaKpiId"
                            className="form-control" 
                            value={this.state.newMetaKpiId}
                            onChange={this.handleInputChange}
                            style={{ borderRadius: '6px', fontSize: '13px' }}
                            required
                          >
                            {kpis.map(k => (
                              <option key={k.id} value={k.id}>{k.nombre}</option>
                            ))}
                          </select>
                        </div>

                        <div className="form-group" style={{ marginBottom: '12px' }}>
                          <label style={{ fontSize: '11px', fontWeight: '600', color: '#64748b' }}>Equipo Responsable</label>
                          <select 
                            name="newMetaEquipoId"
                            className="form-control" 
                            value={this.state.newMetaEquipoId}
                            onChange={this.handleInputChange}
                            style={{ borderRadius: '6px', fontSize: '13px' }}
                            required
                          >
                            {equipos.map(eq => (
                              <option key={eq.id} value={eq.id}>{eq.nombre}</option>
                            ))}
                          </select>
                        </div>

                        <div className="form-group" style={{ marginBottom: '12px' }}>
                          <label style={{ fontSize: '11px', fontWeight: '600', color: '#64748b' }}>Valor Objetivo</label>
                          <input 
                            type="number" 
                            step="any"
                            name="newMetaValor"
                            value={this.state.newMetaValor}
                            onChange={this.handleInputChange}
                            className="form-control" 
                            placeholder="Ej: 25000000 o 95"
                            style={{ borderRadius: '6px', fontSize: '13px' }}
                            required
                          />
                        </div>

                        <div className="form-group" style={{ marginBottom: '20px' }}>
                          <label style={{ fontSize: '11px', fontWeight: '600', color: '#64748b' }}>Fecha Límite</label>
                          <input 
                            type="date" 
                            name="newMetaFecha"
                            value={this.state.newMetaFecha}
                            onChange={this.handleInputChange}
                            className="form-control" 
                            style={{ borderRadius: '6px', fontSize: '13px' }}
                            required
                          />
                        </div>

                        <button 
                          type="submit" 
                          disabled={this.state.newMetaLoading}
                          className="btn btn-az-primary btn-block"
                          style={{ borderRadius: '6px', padding: '10px', fontWeight: '600', backgroundColor: '#5b47fb', borderColor: '#5b47fb' }}
                        >
                          {this.state.newMetaLoading ? 'Creando...' : 'Establecer Objetivo'}
                        </button>
                      </form>
                    </div>
                  ) : (
                    <div className="card" style={{ border: 'none', borderRadius: '10px', boxShadow: '0 4px 12px rgba(0,0,0,0.03)', padding: '30px', height: '100%', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', textAlign: 'center', backgroundColor: '#f8fafc' }}>
                      <div style={{ fontSize: '40px', marginBottom: '15px' }}>🔒</div>
                      <h6 style={{ fontSize: '15px', fontWeight: '700', color: '#1c273c' }}>Acceso Restringido</h6>
                      <p style={{ fontSize: '13px', color: '#64748b', maxWidth: '240px', margin: '0 auto' }}>
                        Solo los roles **Administrador** y **Jefe de Área** están autorizados para definir nuevas metas organizacionales.
                      </p>
                    </div>
                  )}
                </div>
              </div>

              {/* Goal Compliance Report Table */}
              <div className="row row-sm">
                <div className="col-12">
                  <div className="card card-table-one" style={{ border: 'none', borderRadius: '10px', boxShadow: '0 4px 12px rgba(0,0,0,0.03)', padding: '20px' }}>
                    <div style={{ marginBottom: '20px' }}>
                      <h6 className="card-title" style={{ fontSize: '16px', fontWeight: '700', color: '#1c273c', margin: '0' }}>
                        Informe de Cumplimiento de Metas
                      </h6>
                      <p className="card-text" style={{ fontSize: '13px', color: '#7987a1', marginTop: '3px' }}>
                        Estado en tiempo real de los objetivos asignados a cada equipo de trabajo.
                      </p>
                    </div>

                    <div className="table-responsive">
                      <table className="table" style={{ verticalAlign: 'middle' }}>
                        <thead>
                          <tr>
                            <th style={{ color: '#475569', fontWeight: '700', fontSize: '11px', textTransform: 'uppercase' }}>KPI</th>
                            <th style={{ color: '#475569', fontWeight: '700', fontSize: '11px', textTransform: 'uppercase' }}>Tipo</th>
                            <th style={{ color: '#475569', fontWeight: '700', fontSize: '11px', textTransform: 'uppercase' }}>Equipo</th>
                            <th style={{ color: '#475569', fontWeight: '700', fontSize: '11px', textTransform: 'uppercase', textAlign: 'right' }}>Meta</th>
                            <th style={{ color: '#475569', fontWeight: '700', fontSize: '11px', textTransform: 'uppercase', textAlign: 'right' }}>Valor Actual</th>
                            <th style={{ color: '#475569', fontWeight: '700', fontSize: '11px', textTransform: 'uppercase', textAlign: 'center' }}>Progreso</th>
                            <th style={{ color: '#475569', fontWeight: '700', fontSize: '11px', textTransform: 'uppercase', textAlign: 'center' }}>Estado</th>
                            {['ADMIN', 'JEFE_AREA'].includes(role) && (
                              <th style={{ color: '#475569', fontWeight: '700', fontSize: '11px', textTransform: 'uppercase', textAlign: 'center' }}>Acciones</th>
                            )}
                          </tr>
                        </thead>
                        <tbody>
                          {metasReporte && metasReporte.length > 0 ? (
                            metasReporte.map((meta) => {
                              const isEditing = this.state.editingMetaId === meta.id;
                              return (
                                <tr key={meta.id}>
                                  <td style={{ fontWeight: '600', color: '#1e293b' }}>{meta.kpiNombre}</td>
                                  <td>
                                    <span style={{ fontSize: '10px', fontWeight: '700', padding: '3px 8px', borderRadius: '4px', backgroundColor: meta.kpiTipo === 'FINANCIERO' ? '#eff6ff' : meta.kpiTipo === 'CUMPLIMIENTO' ? '#ecfdf5' : '#fff7ed', color: meta.kpiTipo === 'FINANCIERO' ? '#2563eb' : meta.kpiTipo === 'CUMPLIMIENTO' ? '#059669' : '#ea580c' }}>
                                      {meta.kpiTipo}
                                    </span>
                                  </td>
                                  <td style={{ color: '#475569', fontWeight: '500' }}>{meta.equipoNombre}</td>
                                  
                                  {/* Meta objective input or display */}
                                  <td style={{ fontWeight: '700', color: '#1e293b', textAlign: 'right' }}>
                                    {isEditing ? (
                                      <input 
                                        type="number" 
                                        name="editMetaValor"
                                        value={this.state.editMetaValor}
                                        onChange={this.handleInputChange}
                                        className="form-control form-control-sm"
                                        style={{ width: '120px', display: 'inline-block', borderRadius: '4px', fontSize: '12px' }}
                                      />
                                    ) : (
                                      `${meta.valorObjetivo.toLocaleString()} ${meta.unidadMedida}`
                                    )}
                                  </td>

                                  <td style={{ fontWeight: '700', color: '#1e293b', textAlign: 'right' }}>
                                    {meta.valorActual.toLocaleString()} {meta.unidadMedida}
                                  </td>
                                  
                                  <td style={{ width: '180px' }}>
                                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                                      <div className="progress" style={{ height: '6px', flex: '1', borderRadius: '3px', margin: '0', backgroundColor: '#e2e8f0' }}>
                                        <div 
                                          className="progress-bar" 
                                          role="progressbar" 
                                          style={{ 
                                            width: `${meta.cumplimiento}%`, 
                                            backgroundColor: meta.estado === 'CUMPLIDA' ? '#10b981' : meta.estado === 'EN_PROGRESO' ? '#f59e0b' : '#ef4444',
                                            borderRadius: '3px'
                                          }} 
                                          aria-valuenow={meta.cumplimiento} 
                                          aria-valuemin="0" 
                                          aria-valuemax="100"
                                        ></div>
                                      </div>
                                      <span style={{ fontSize: '12px', fontWeight: '700', color: '#1e293b', minWidth: '40px', textAlign: 'right' }}>
                                        {meta.cumplimiento}%
                                      </span>
                                    </div>
                                  </td>
                                  
                                  {/* Goal state selector or display */}
                                  <td style={{ textAlign: 'center' }}>
                                    {isEditing ? (
                                      <select
                                        name="editMetaEstado"
                                        value={this.state.editMetaEstado}
                                        onChange={this.handleInputChange}
                                        className="form-control form-control-sm"
                                        style={{ width: '110px', borderRadius: '4px', fontSize: '12px', padding: '0 5px' }}
                                      >
                                        <option value="ACTIVA">ACTIVA</option>
                                        <option value="CUMPLIDA">CUMPLIDA</option>
                                      </select>
                                    ) : (
                                      <span className="badge" style={{ 
                                        fontSize: '10px', 
                                        padding: '5px 12px', 
                                        borderRadius: '12px', 
                                        fontWeight: '700',
                                        backgroundColor: meta.estado === 'CUMPLIDA' ? '#ecfdf5' : meta.estado === 'EN_PROGRESO' ? '#fffbeb' : '#fef2f2',
                                        color: meta.estado === 'CUMPLIDA' ? '#059669' : meta.estado === 'EN_PROGRESO' ? '#d97706' : '#dc2626'
                                      }}>
                                        {meta.estado}
                                      </span>
                                    )}
                                  </td>

                                  {/* CRUD actions for autorized roles */}
                                  {['ADMIN', 'JEFE_AREA'].includes(role) && (
                                    <td style={{ textAlign: 'center', whiteSpace: 'nowrap' }}>
                                      {isEditing ? (
                                        <div style={{ display: 'flex', gap: '5px', justifyContent: 'center' }}>
                                          <button 
                                            onClick={() => this.handleUpdateMeta(meta.id)}
                                            className="btn btn-success btn-xs"
                                            style={{ padding: '2px 8px', fontSize: '11px', borderRadius: '4px' }}
                                          >
                                            💾 Guardar
                                          </button>
                                          <button 
                                            onClick={this.cancelEditingMeta}
                                            className="btn btn-secondary btn-xs"
                                            style={{ padding: '2px 8px', fontSize: '11px', borderRadius: '4px' }}
                                          >
                                            ❌ Cancelar
                                          </button>
                                        </div>
                                      ) : (
                                        <div style={{ display: 'flex', gap: '5px', justifyContent: 'center' }}>
                                          <button 
                                            onClick={() => this.startEditingMeta(meta)}
                                            className="btn btn-warning btn-xs text-white"
                                            style={{ padding: '2px 8px', fontSize: '11px', borderRadius: '4px', backgroundColor: '#f59e0b', borderColor: '#f59e0b' }}
                                          >
                                            ✏️ Editar
                                          </button>
                                          <button 
                                            onClick={() => this.handleDeleteMeta(meta.id)}
                                            className="btn btn-danger btn-xs"
                                            style={{ padding: '2px 8px', fontSize: '11px', borderRadius: '4px' }}
                                          >
                                            🗑️ Borrar
                                          </button>
                                        </div>
                                      )}
                                    </td>
                                  )}
                                </tr>
                              );
                            })
                          ) : (
                            <tr>
                              <td colSpan={['ADMIN', 'JEFE_AREA'].includes(role) ? "8" : "7"} style={{ textAlign: 'center', color: '#94a3b8', padding: '30px' }}>
                                No hay metas asignadas disponibles para tu nivel de acceso en este momento.
                              </td>
                            </tr>
                          )}
                        </tbody>
                      </table>
                    </div>
                  </div>
                </div>
              </div>
            </>
          ) : (
            /* User Management Tab Content (Admin Only) */
            <div className="row row-sm">
              {/* Form to create a new user */}
              <div className="col-lg-4">
                <div className="card" style={{ border: 'none', borderRadius: '10px', boxShadow: '0 4px 12px rgba(0,0,0,0.03)', padding: '25px 20px' }}>
                  <h6 style={{ fontSize: '16px', fontWeight: '700', color: '#1c273c', marginBottom: '20px' }}>
                    {this.state.editingUserUsername ? `✏️ Editar Colaborador @${this.state.editingUserUsername}` : '👤 Crear Nuevo Colaborador'}
                  </h6>
                  
                  {this.state.newUsrSuccess && (
                    <div className="alert alert-success" style={{ fontSize: '12px', padding: '8px 12px', borderRadius: '6px', marginBottom: '15px' }}>
                      {this.state.newUsrSuccess}
                    </div>
                  )}
                  {this.state.newUsrError && (
                    <div className="alert alert-danger" style={{ fontSize: '12px', padding: '8px 12px', borderRadius: '6px', marginBottom: '15px' }}>
                      {this.state.newUsrError}
                    </div>
                  )}

                  <form onSubmit={this.handleCreateUsuario}>
                    <div className="form-group" style={{ marginBottom: '15px' }}>
                      <label style={{ fontSize: '11px', fontWeight: '600', color: '#64748b' }}>Nombre Completo</label>
                      <input 
                        type="text" 
                        name="newUsrNombreCompleto"
                        value={this.state.newUsrNombreCompleto}
                        onChange={this.handleInputChange}
                        className="form-control" 
                        placeholder="Ej: Carlos Silva"
                        style={{ borderRadius: '6px', fontSize: '13px' }}
                        required
                      />
                    </div>

                    <div className="form-group" style={{ marginBottom: '15px' }}>
                      <label style={{ fontSize: '11px', fontWeight: '600', color: '#64748b' }}>Nombre de Usuario (Login)</label>
                      <input 
                        type="text" 
                        name="newUsrUsername"
                        value={this.state.newUsrUsername}
                        onChange={this.handleInputChange}
                        className="form-control" 
                        placeholder="Ej: carlos.ventas"
                        style={{ borderRadius: '6px', fontSize: '13px' }}
                        required
                        disabled={this.state.editingUserUsername !== null}
                      />
                    </div>

                    <div className="form-group" style={{ marginBottom: '15px' }}>
                      <label style={{ fontSize: '11px', fontWeight: '600', color: '#64748b' }}>Contraseña</label>
                      <input 
                        type="password" 
                        name="newUsrPassword"
                        value={this.state.newUsrPassword}
                        onChange={this.handleInputChange}
                        className="form-control" 
                        placeholder={this.state.editingUserUsername ? "Dejar vacío para mantener" : "Contraseña"}
                        style={{ borderRadius: '6px', fontSize: '13px' }}
                        required={this.state.editingUserUsername === null}
                      />
                    </div>

                    <div className="form-group" style={{ marginBottom: '15px' }}>
                      <label style={{ fontSize: '11px', fontWeight: '600', color: '#64748b' }}>Rol del Sistema</label>
                      <select 
                        name="newUsrRole"
                        className="form-control" 
                        value={this.state.newUsrRole}
                        onChange={this.handleInputChange}
                        style={{ borderRadius: '6px', fontSize: '13px' }}
                        required
                      >
                        <option value="COLABORADOR">Colaborador (Ej: Vendedor/Operario)</option>
                        <option value="JEFE_AREA">Jefe de Área</option>
                        <option value="GERENTE">Gerente</option>
                        <option value="ADMIN">Administrador</option>
                      </select>
                    </div>

                    {/* Optional Area & Team selection (only relevant if role is COLABORADOR or JEFE_AREA) */}
                    {['COLABORADOR', 'JEFE_AREA'].includes(this.state.newUsrRole) && (
                      <>
                        <div className="form-group" style={{ marginBottom: '15px' }}>
                          <label style={{ fontSize: '11px', fontWeight: '600', color: '#64748b' }}>Área Asignada</label>
                          <select 
                            name="newUsrAreaId"
                            className="form-control" 
                            value={this.state.newUsrAreaId}
                            onChange={this.handleInputChange}
                            style={{ borderRadius: '6px', fontSize: '13px' }}
                          >
                            <option value="">Ninguna</option>
                            {areas.map(a => (
                              <option key={a.id} value={a.id}>{a.nombre}</option>
                            ))}
                          </select>
                        </div>

                        {this.state.newUsrRole === 'COLABORADOR' && (
                          <div className="form-group" style={{ marginBottom: '20px' }}>
                            <label style={{ fontSize: '11px', fontWeight: '600', color: '#64748b' }}>Equipo de Trabajo</label>
                            <select 
                              name="newUsrEquipoId"
                              className="form-control" 
                              value={this.state.newUsrEquipoId}
                              onChange={this.handleInputChange}
                              style={{ borderRadius: '6px', fontSize: '13px' }}
                            >
                              <option value="">Ninguno</option>
                              {equipos.map(eq => (
                                <option key={eq.id} value={eq.id}>{eq.nombre}</option>
                              ))}
                            </select>
                          </div>
                        )}
                      </>
                    )}

                    <button 
                      type="submit" 
                      disabled={this.state.newUsrSubmitLoading}
                      className="btn btn-az-primary btn-block"
                      style={{ borderRadius: '6px', padding: '10px', fontWeight: '600', backgroundColor: '#5b47fb', borderColor: '#5b47fb' }}
                    >
                      {this.state.newUsrSubmitLoading ? 'Guardando...' : (this.state.editingUserUsername ? '✏️ Guardar Cambios' : 'Crear Cuenta')}
                    </button>

                    {this.state.editingUserUsername && (
                      <button 
                        type="button" 
                        onClick={this.handleCancelEditUsuario}
                        className="btn btn-outline-light btn-block"
                        style={{ borderRadius: '6px', padding: '10px', fontWeight: '600', marginTop: '10px', border: '1px solid #cdd4e0', color: '#495057' }}
                      >
                        Cancelar Edición
                      </button>
                    )}
                  </form>
                </div>
              </div>

              {/* List of existing database users */}
              <div className="col-lg-8 mg-t-20 mg-lg-t-0">
                <div className="card card-table-one" style={{ border: 'none', borderRadius: '10px', boxShadow: '0 4px 12px rgba(0,0,0,0.03)', padding: '20px' }}>
                  <div style={{ marginBottom: '20px' }}>
                    <h6 className="card-title" style={{ fontSize: '16px', fontWeight: '700', color: '#1c273c', margin: '0' }}>
                      Usuarios en Base de Datos MySQL (areas_db)
                    </h6>
                    <p className="card-text" style={{ fontSize: '13px', color: '#7987a1', marginTop: '3px' }}>
                      Lista de cuentas registradas en el microservicio de Estructura Organizativa.
                    </p>
                  </div>

                  {this.state.usuariosLoading ? (
                    <p>Cargando lista...</p>
                  ) : this.state.usuariosError ? (
                    <p className="text-danger">{this.state.usuariosError}</p>
                  ) : (
                    <div className="table-responsive">
                      <table className="table" style={{ verticalAlign: 'middle' }}>
                        <thead>
                          <tr>
                            <th style={{ color: '#475569', fontWeight: '700', fontSize: '11px', textTransform: 'uppercase', padding: '12px 16px' }}>Nombre Completo</th>
                            <th style={{ color: '#475569', fontWeight: '700', fontSize: '11px', textTransform: 'uppercase', padding: '12px 16px' }}>Usuario</th>
                            <th style={{ color: '#475569', fontWeight: '700', fontSize: '11px', textTransform: 'uppercase', padding: '12px 16px' }}>Rol</th>
                            <th style={{ color: '#475569', fontWeight: '700', fontSize: '11px', textTransform: 'uppercase', padding: '12px 16px' }}>Área/Equipo</th>
                            <th style={{ color: '#475569', fontWeight: '700', fontSize: '11px', textTransform: 'uppercase', textAlign: 'center', padding: '12px 16px' }}>Acciones</th>
                          </tr>
                        </thead>
                        <tbody>
                          {this.state.usuariosList.map(u => {
                            const userArea = areas.find(a => a.id === u.areaId);
                            const userEquipo = equipos.find(eq => eq.id === u.equipoId);
                            return (
                              <tr key={u.username}>
                                <td style={{ fontWeight: '600', color: '#1e293b', padding: '16px 12px' }}>{u.nombreCompleto}</td>
                                <td style={{ color: '#64748b', padding: '16px 12px' }}>@{u.username}</td>
                                <td style={{ padding: '16px 12px' }}>
                                  <span className="badge" style={{ fontSize: '9px', fontWeight: '700', padding: '3px 8px', borderRadius: '4px', backgroundColor: u.role === 'ADMIN' ? '#fef2f2' : u.role === 'GERENTE' ? '#eff6ff' : u.role === 'JEFE_AREA' ? '#fdf2f8' : '#f8fafc', color: u.role === 'ADMIN' ? '#dc2626' : u.role === 'GERENTE' ? '#2563eb' : u.role === 'JEFE_AREA' ? '#db2777' : '#475569' }}>
                                    {u.role}
                                  </span>
                                </td>
                                <td style={{ fontSize: '12px', padding: '16px 12px' }}>
                                  {userArea ? userArea.nombre : 'Global'}
                                  {userEquipo ? ` / ${userEquipo.nombre}` : ''}
                                </td>
                                <td style={{ textAlign: 'center', padding: '16px 12px', whiteSpace: 'nowrap' }}>
                                  <div style={{ display: 'flex', gap: '8px', justifyContent: 'center', alignItems: 'center' }}>
                                    <button 
                                      onClick={() => this.handleEditUsuarioClick(u)}
                                      className="btn btn-outline-primary btn-xs"
                                      style={{ padding: '4px 10px', fontSize: '11px', borderRadius: '6px', border: '1px solid #5b47fb', color: '#5b47fb', backgroundColor: 'transparent' }}
                                    >
                                      ✏️ Editar
                                    </button>
                                    <button 
                                      onClick={() => this.handleDeleteUsuario(u.username)}
                                      disabled={u.username === 'admin'}
                                      className="btn btn-outline-danger btn-xs"
                                      style={{ padding: '4px 10px', fontSize: '11px', borderRadius: '6px' }}
                                    >
                                      🗑️ Borrar
                                    </button>
                                  </div>
                                </td>
                              </tr>
                            );
                          })}
                        </tbody>
                      </table>
                    </div>
                  )}
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    );
  }
}

export default Dashboard
