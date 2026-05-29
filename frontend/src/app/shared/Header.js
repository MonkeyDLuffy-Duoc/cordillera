import React, { Component } from "react";
import { Link, withRouter } from "react-router-dom";
import { Dropdown } from "react-bootstrap";

export class Header extends Component {
  closeMenu(e) {
    if (e.target.closest(".dropdown")) {
      e.target.closest(".dropdown").classList.remove("show");
    }
    if (e.target.closest(".dropdown .dropdown-menu")) {
      e.target.closest(".dropdown .dropdown-menu").classList.remove("show");
    }
  }

  toggleHeaderMenu(e) {
    e.preventDefault();
    document.querySelector("body").classList.toggle("az-header-menu-show");
  }

  componentDidUpdate(prevProps) {
    if (this.props.location !== prevProps.location) {
      document.querySelector("body").classList.remove("az-header-menu-show");
    }
  }

  handleSignOut = (e) => {
    e.preventDefault();
    localStorage.clear();
    this.props.history.push('/general-pages/signin');
  }

  render() {
    const nombreCompleto = localStorage.getItem('nombreCompleto') || 'Usuario Demo';
    const role = localStorage.getItem('role') || 'Invitado';

    return (
      <div>
        <div className="az-header" style={{ borderBottom: '1px solid #e2e8f0', backgroundColor: '#ffffff' }}>
          <div className="container">
            <div className="az-header-left">
              <Link to="/dashboard" className="az-logo" style={{ color: '#5b47fb', fontSize: '24px', fontWeight: 'bold' }}>
                grupo<span>c</span>ordillera
              </Link>
              <a
                id="azMenuShow"
                onClick={event => this.toggleHeaderMenu(event)}
                className="az-header-menu-icon d-lg-none"
                href="#/"
              >
                <span></span>
              </a>
            </div>
            <div className="az-header-menu">
              <div className="az-header-menu-header">
                <Link to="/dashboard" className="az-logo" style={{ color: '#5b47fb' }}>
                  grupo<span>c</span>ordillera
                </Link>
                <a
                  href="#/"
                  onClick={event => this.toggleHeaderMenu(event)}
                  className="close"
                >
                  &times;
                </a>
              </div>
              <ul className="nav">
                <li
                  className={
                    this.isPathActive("/dashboard")
                      ? "nav-item active"
                      : "nav-item"
                  }
                >
                  <Link to="/dashboard" className="nav-link">
                    <i className="typcn typcn-chart-area-outline"></i> Monitoreo KPIs
                  </Link>
                </li>
              </ul>
            </div>
            <div className="az-header-right">
              <div style={{ marginRight: '15px', textAlign: 'right', display: 'none' }} className="d-sm-block">
                <span style={{ fontSize: '11px', fontWeight: '600', color: '#5b47fb', backgroundColor: '#eef2ff', padding: '3px 8px', borderRadius: '12px', textTransform: 'uppercase' }}>
                  Rol: {role}
                </span>
              </div>
              
              <Dropdown className="az-profile-menu">
                <Dropdown.Toggle as={"a"} className="az-img-user" style={{ cursor: 'pointer' }}>
                  <img
                    src={require("../../assets/images/img1.jpg")}
                    alt="Profile"
                  ></img>
                </Dropdown.Toggle>
                <Dropdown.Menu>
                  <div className="az-dropdown-header d-sm-none">
                    <a
                      href="#/"
                      onClick={event => this.closeMenu(event)}
                      className="az-header-arrow"
                    >
                      <i className="icon ion-md-arrow-back"></i>
                    </a>
                  </div>
                  <div className="az-header-profile" style={{ padding: '20px', borderBottom: '1px solid #f1f5f9' }}>
                    <div className="az-img-user" style={{ marginBottom: '10px' }}>
                      <img
                        src={require("../../assets/images/img1.jpg")}
                        alt="Profile"
                      ></img>
                    </div>
                    <h6 style={{ fontWeight: '600', color: '#1c273c', margin: '0' }}>{nombreCompleto}</h6>
                    <span style={{ fontSize: '12px', color: '#5b47fb', fontWeight: '600' }}>{role}</span>
                  </div>

                  <a 
                    href="#/" 
                    onClick={this.handleSignOut} 
                    className="dropdown-item" 
                    style={{ padding: '12px 20px', color: '#ea580c', fontWeight: '600', cursor: 'pointer' }}
                  >
                    <i className="typcn typcn-power-outline" style={{ color: '#ea580c' }}></i> Cerrar Sesión
                  </a>
                </Dropdown.Menu>
              </Dropdown>
            </div>
          </div>
        </div>
      </div>
    );
  }

  isPathActive(path) {
    return this.props.location.pathname.startsWith(path);
  }
}

export default withRouter(Header);
